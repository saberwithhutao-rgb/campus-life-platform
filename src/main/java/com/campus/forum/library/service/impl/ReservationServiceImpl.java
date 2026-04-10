package com.campus.forum.library.service.impl;

import com.campus.forum.library.entity.Reservation;
import com.campus.forum.library.dto.ReservationDTO;
import com.campus.forum.library.repository.ReservationRepository;
import com.campus.forum.library.service.ReservationService;
import com.campus.forum.library.service.SeatService;
import com.campus.forum.library.util.TimeUtils;
import com.campus.forum.common.util.RedisDistributedLockUtil;
import com.campus.forum.common.util.RedisRateLimiterUtil;
import com.campus.forum.common.util.RedisCacheUtil;
import com.campus.forum.user.entity.User;
import com.campus.forum.user.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ReservationServiceImpl implements ReservationService {
    private static final Logger log = LoggerFactory.getLogger(ReservationServiceImpl.class);
    private final ReservationRepository reservationRepository;
    private final SeatService seatService;
    private final UserRepository userRepository;

    @Resource
    private RedisDistributedLockUtil redisDistributedLockUtil;
    @Resource
    private RedisRateLimiterUtil redisRateLimiterUtil;
    @Resource
    private RedisCacheUtil redisCacheUtil;

    public ReservationServiceImpl(ReservationRepository reservationRepository,
                                  SeatService seatService,
                                  UserRepository userRepository) {
        this.reservationRepository = reservationRepository;
        this.seatService = seatService;
        this.userRepository = userRepository;
    }

    // ==================== 预约相关方法 ====================

    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public Reservation createReservation(ReservationDTO reservationDTO, Integer userId) {
        String userIdStr = userId.toString();

        if (!redisRateLimiterUtil.checkCreateLimit(userIdStr)) {
            throw new RuntimeException("操作过于频繁，请稍后再试");
        }

        // 检查用户活跃预约数量
        checkUserActiveReservationLimit(userId);

        // ========== 新增：时间边界校验 ==========
        LocalTime startTime = reservationDTO.getStartTime();
        LocalTime endTime = reservationDTO.getEndTime();
        LocalTime CLOSING_TIME = LocalTime.of(23, 0);  // 23:00 关门

        // 1. 开始时间必须在 07:00 - 22:00 之间（因为至少要预约1小时）
        if (startTime.isBefore(LocalTime.of(7, 0))) {
            throw new RuntimeException("图书馆开放时间为 07:00 - 23:00");
        }
        if (startTime.isAfter(LocalTime.of(22, 0))) {
            throw new RuntimeException("最晚只能预约到 23:00，请选择更早的开始时间");
        }

        // 2. 结束时间不能超过 23:00
        if (endTime.isAfter(CLOSING_TIME)) {
            throw new RuntimeException("预约时间不能超过 23:00");
        }

        // 3. 检查预约时间是否合法（不能预约过去的时间）
        if (!TimeUtils.isReservationTimeValid(reservationDTO.getReserveDate(), startTime)) {
            throw new RuntimeException("不能预约已过去的时间段");
        }

        String lockKey = redisDistributedLockUtil.generateLockKey(
                "library:seat",
                reservationDTO.getSeatId().toString(),
                reservationDTO.getReserveDate() + "-" + reservationDTO.getStartTime());
        String requestId = UUID.randomUUID().toString();
        int expireTime = 30;

        try {
            if (!redisDistributedLockUtil.acquireLock(lockKey, requestId, expireTime)) {
                throw new RuntimeException("系统繁忙，请稍后再试");
            }

            // 检查冲突预约
            List<Reservation> conflictingReservations = reservationRepository.findConflictingReservations(
                    reservationDTO.getSeatId(),
                    reservationDTO.getReserveDate(),
                    reservationDTO.getStartTime(),
                    reservationDTO.getEndTime());

            if (!conflictingReservations.isEmpty()) {
                throw new RuntimeException("该时间段已被预约");
            }

            // 创建新预约
            Reservation reservation = new Reservation();
            reservation.setUserId(userId);
            reservation.setSeatId(reservationDTO.getSeatId());
            reservation.setClassroomId(reservationDTO.getClassroomId());
            reservation.setReserveDate(reservationDTO.getReserveDate());
            reservation.setStartTime(reservationDTO.getStartTime());
            reservation.setDuration(reservationDTO.getDuration());
            reservation.setEndTime(TimeUtils.calculateEndTime(reservationDTO.getStartTime(), reservationDTO.getDuration()));
            reservation.setType("reservation");
            reservation.setStatus("active");
            reservation.setCreatedAt(LocalDateTime.now());

            Reservation savedReservation = reservationRepository.save(reservation);
            seatService.updateSeatStatus(reservationDTO.getSeatId(), "reserved");
            redisCacheUtil.clearReservationCache(reservationDTO.getSeatId().longValue(),
                    reservationDTO.getReserveDate().toString());

            return savedReservation;
        } catch (ObjectOptimisticLockingFailureException e) {
            throw new RuntimeException("操作失败：该座位信息已被其他用户修改，请刷新页面后重试");
        } finally {
            redisDistributedLockUtil.releaseLock(lockKey, requestId);
        }
    }

    @Override
    @Transactional
    public Reservation occupySeat(Integer reservationId, Integer userId) {
        String userIdStr = userId.toString();

        if (!redisRateLimiterUtil.checkOccupyLimit(userIdStr)) {
            throw new RuntimeException("操作过于频繁，请稍后再试");
        }

        checkUserActiveReservationLimit(userId);

        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("预约不存在"));

        if (!reservation.getStatus().equals("active")) {
            throw new RuntimeException("该预约已被处理");
        }

        if (reservation.getUserId().equals(userId)) {
            throw new RuntimeException("不能占用自己的预约");
        }

        if (!TimeUtils.isAfter30MinutesFromStart(reservation.getReserveDate(), reservation.getStartTime())) {
            throw new RuntimeException("预约开始时间后30分钟才能占用");
        }

        String lockKey = redisDistributedLockUtil.generateLockKey(
                "library:seat",
                reservation.getSeatId().toString(),
                reservation.getReserveDate() + "-" + reservation.getStartTime());
        String requestId = UUID.randomUUID().toString();
        int expireTime = 30;

        try {
            if (!redisDistributedLockUtil.acquireLock(lockKey, requestId, expireTime)) {
                throw new RuntimeException("系统繁忙，请稍后再试");
            }

            Integer seatId = reservation.getSeatId();
            String seatStatus = seatService.getSeatStatus(seatId);

            log.info("占用前检查：预约ID={}, 预约状态={}, 座位ID={}, 座位状态={}",
                    reservationId, reservation.getStatus(), seatId, seatStatus);

            if (!seatStatus.equals("reserved")) {
                throw new RuntimeException("座位状态不正确，当前状态为：" + seatStatus + "，仅 reserved 状态可被占用");
            }

            Reservation occupation = new Reservation();
            occupation.setUserId(userId);
            occupation.setSeatId(reservation.getSeatId());
            occupation.setClassroomId(reservation.getClassroomId());
            occupation.setReserveDate(reservation.getReserveDate());
            occupation.setStartTime(reservation.getStartTime());
            occupation.setDuration(reservation.getDuration());
            occupation.setEndTime(reservation.getEndTime());
            occupation.setType("occupation");
            occupation.setStatus("active");
            occupation.setCreatedAt(LocalDateTime.now());

            Reservation savedOccupation = reservationRepository.save(occupation);

            reservation.setStatus("replaced");
            reservation.setActualEndTime(LocalDateTime.now());
            reservationRepository.save(reservation);

            seatService.updateSeatStatus(reservation.getSeatId(), "occupied");
            redisCacheUtil.clearReservationCache(reservation.getSeatId().longValue(),
                    reservation.getReserveDate().toString());

            return savedOccupation;
        } catch (ObjectOptimisticLockingFailureException e) {
            throw new RuntimeException("操作失败：该座位信息已被其他用户修改，请刷新页面后重试");
        } finally {
            redisDistributedLockUtil.releaseLock(lockKey, requestId);
        }
    }

    @Override
    @Transactional
    public void leaveSeat(Integer reservationId, Integer userId) {
        try {
            Reservation reservation = reservationRepository.findById(reservationId)
                    .orElseThrow(() -> new RuntimeException("预约不存在"));

            if (!reservation.getUserId().equals(userId)) {
                throw new RuntimeException("只能离开自己的预约");
            }

            if (!reservation.getStatus().equals("active")) {
                throw new RuntimeException("该预约已结束或已取消");
            }

            reservation.setStatus("cancelled");
            reservation.setActualEndTime(LocalDateTime.now());
            long actualDuration = java.time.Duration.between(reservation.getCreatedAt(), LocalDateTime.now()).toMinutes();
            reservation.setActualDurationMinutes((int) actualDuration);
            reservationRepository.save(reservation);

            seatService.updateSeatStatus(reservation.getSeatId(), "available");
            redisCacheUtil.clearReservationCache(reservation.getSeatId().longValue(),
                    reservation.getReserveDate().toString());
        } catch (ObjectOptimisticLockingFailureException e) {
            throw new RuntimeException("操作失败：该座位信息已被其他用户修改，请刷新页面后重试");
        }
    }

    @Override
    public List<Reservation> getReservationsByUserId(Integer userId) {
        return reservationRepository.findByUserId(userId);
    }

    /**
     * ✅ 批量查询优化版 - 获取座位的所有预约
     */
    @Override
    public List<Reservation> getReservationBySeatId(Integer seatId, Integer currentUserId) {
        // 1. 获取该座位所有活跃的预约
        List<Reservation> reservations = reservationRepository.findBySeatIdAndStatus(seatId, "active");

        if (reservations.isEmpty()) {
            log.debug("座位 {} 没有活跃预约", seatId);
            return reservations;
        }

        // 2. 批量获取所有预约的用户ID（去重）
        List<Integer> userIds = reservations.stream()
                .map(Reservation::getUserId)
                .distinct()
                .collect(Collectors.toList());

        log.debug("座位 {} 有 {} 条预约，涉及 {} 个用户", seatId, reservations.size(), userIds.size());

        // 3. 批量查询用户信息（一次查询获取所有用户）
        Map<Integer, User> userMap = userRepository.findAllById(userIds)
                .stream()
                .collect(Collectors.toMap(User::getId, user -> user));

        // 4. 填充预约数据
        for (Reservation reservation : reservations) {
            Integer userId = reservation.getUserId();
            User user = userMap.get(userId);

            // 设置用户名
            if (user != null && user.getUsername() != null) {
                reservation.setUserName(user.getUsername());
            } else {
                reservation.setUserName("用户" + userId);  // 兜底
            }

            // 设置是否是当前用户
            reservation.setIsOwner(currentUserId != null && currentUserId.equals(userId));
        }

        log.info("获取座位 {} 的预约列表成功，共 {} 条", seatId, reservations.size());

        return reservations;
    }

    @Override
    @Scheduled(cron = "0 * * * * *")
    @Transactional
    public void processExpiredReservations() {
        List<Reservation> expiredReservations = reservationRepository.findExpiredReservations();
        for (Reservation reservation : expiredReservations) {
            reservation.setStatus("completed");
            reservation.setActualEndTime(LocalDateTime.now());
            reservationRepository.save(reservation);
            seatService.updateSeatStatus(reservation.getSeatId(), "available");
        }

        if (!expiredReservations.isEmpty()) {
            log.info("定时任务：自动释放了 {} 条过期预约", expiredReservations.size());
        }
    }

    // ==================== 私有辅助方法 ====================

    private void checkUserActiveReservationLimit(Integer userId) {
        int activeCount = reservationRepository.countActiveReservationsByUserId(userId);
        if (activeCount >= 3) {
            throw new RuntimeException("您最多只能同时预约/占用3个座位，请完成现有预约后再操作");
        }
    }
}