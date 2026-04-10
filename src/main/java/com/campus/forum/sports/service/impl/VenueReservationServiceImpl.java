package com.campus.forum.sports.service.impl;

import com.campus.forum.sports.entity.VenueReservation;
import com.campus.forum.sports.entity.Court;
import com.campus.forum.sports.dto.VenueReservationDTO;
import com.campus.forum.sports.repository.VenueReservationRepository;
import com.campus.forum.sports.repository.CourtRepository;
import com.campus.forum.sports.service.VenueReservationService;
import com.campus.forum.sports.service.CourtService;
import com.campus.forum.common.util.RedisDistributedLockUtil;
import com.campus.forum.common.util.RedisRateLimiterUtil;
import com.campus.forum.common.util.RedisCacheUtil;
import com.campus.forum.user.entity.User;
import com.campus.forum.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import jakarta.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class VenueReservationServiceImpl implements VenueReservationService {
  private final VenueReservationRepository reservationRepository;
  private final CourtRepository courtRepository;
  private final CourtService courtService;
    private final UserRepository userRepository;

  @Resource
  private RedisDistributedLockUtil redisDistributedLockUtil;
  @Resource
  private RedisRateLimiterUtil redisRateLimiterUtil;
  @Resource
  private RedisCacheUtil redisCacheUtil;

  public VenueReservationServiceImpl(VenueReservationRepository reservationRepository, CourtRepository courtRepository,
                                     CourtService courtService, UserRepository userRepository) {
    this.reservationRepository = reservationRepository;
    this.courtRepository = courtRepository;
    this.courtService = courtService;
      this.userRepository = userRepository;
  }

  @Override
  @Transactional
  public VenueReservation createReservation(VenueReservationDTO reservationDTO, Integer userId) {
    // 检查限流 - 高并发预约模块专用
    if (!redisRateLimiterUtil.checkReservationRateLimit(userId.toString())) {
      throw new IllegalArgumentException("请求过于频繁，请稍后再试");
    }

    long activeCount = reservationRepository.countActiveReservationsByUserId(userId);
    if (activeCount >= 3) {
      throw new IllegalArgumentException("您最多只能同时预约3个场地，请先完成或取消后再预约");
    }
      // 解析日期和时间
      LocalDate reserveDate;
      LocalTime startTime;
      LocalTime endTime;

      try {
          reserveDate = LocalDate.parse(reservationDTO.getReserveDate());
          startTime = LocalTime.parse(reservationDTO.getStartTime());
          endTime = LocalTime.parse(reservationDTO.getEndTime());
      } catch (DateTimeParseException e) {
          e.printStackTrace();
          throw new IllegalArgumentException("时间格式不正确");
      }

      // ========== 新增：时间边界校验 ==========
      LocalTime OPENING_TIME = LocalTime.of(7, 0);   // 07:00 开门
      LocalTime CLOSING_TIME = LocalTime.of(23, 0);  // 23:00 关门

      // 1. 开始时间必须在 07:00 - 22:00 之间（因为至少要预约1小时）
      if (startTime.isBefore(OPENING_TIME)) {
          throw new IllegalArgumentException("体育馆开放时间为 07:00 - 23:00");
      }
      if (startTime.isAfter(LocalTime.of(22, 0))) {
          throw new IllegalArgumentException("最晚只能预约到 23:00，请选择更早的开始时间");
      }

      // 2. 结束时间不能超过 23:00
      if (endTime.isAfter(CLOSING_TIME)) {
          throw new IllegalArgumentException("预约时间不能超过 23:00");
      }

      // 3. 检查开始时间是否晚于结束时间（跨天检测）
      if (endTime.isBefore(startTime)) {
          throw new IllegalArgumentException("预约时间不能跨天，必须在 23:00 前结束");
      }
      // ====================================

    // 检查时间是否合法
    LocalDateTime now = LocalDateTime.now();
    LocalDateTime reservationTime = LocalDateTime.of(reserveDate, startTime);
    if (reservationTime.isBefore(now)) {
      throw new IllegalArgumentException("预约时间不能早于当前时间");
    }

    // 生成分布式锁键 - 高并发预约模块专用
    String lockKey = redisDistributedLockUtil.generateLockKey(
        "sports:court",
        reservationDTO.getCourtId().toString(),
        reservationDTO.getReserveDate() + "-" + reservationDTO.getStartTime());
    String requestId = UUID.randomUUID().toString();
    int expireTime = 30; // 锁过期时间30秒

    try {
      // 获取分布式锁 - 高并发预约模块专用
      if (!redisDistributedLockUtil.acquireLock(lockKey, requestId, expireTime)) {
        throw new IllegalArgumentException("系统繁忙，请稍后再试");
      }

      // 检查时间重叠
      List<VenueReservation> overlappingReservations = reservationRepository.findOverlappingReservations(
          reservationDTO.getCourtId(), reserveDate, startTime, endTime);
      if (!overlappingReservations.isEmpty()) {
        throw new IllegalArgumentException("该时间段已被预约");
      }

      // 创建预约
      VenueReservation reservation = new VenueReservation();
      reservation.setUserId(userId);
      reservation.setCourtId(reservationDTO.getCourtId());
      reservation.setVenueId(reservationDTO.getVenueId());
      reservation.setReserveDate(reserveDate);
      reservation.setStartTime(startTime);
      reservation.setDuration(reservationDTO.getDuration());
      reservation.setEndTime(endTime);
      reservation.setType("reservation");
      reservation.setStatus("active");
      reservation.setCreatedAt(LocalDateTime.now());

      // 保存预约
      VenueReservation savedReservation = reservationRepository.save(reservation);

      // 更新场地状态
      courtService.updateCourtStatus(reservationDTO.getCourtId(), "reserved");

      // 清除缓存 - 高并发预约模块专用
      redisCacheUtil.clearVenueReservationCache(reservationDTO.getCourtId().longValue(),
          reservationDTO.getReserveDate());

      return savedReservation;
    } catch (ObjectOptimisticLockingFailureException e) {
      throw new IllegalArgumentException("操作失败：该场地信息已被其他用户修改，请刷新页面后重试");
    } finally {
      // 释放分布式锁 - 高并发预约模块专用
      redisDistributedLockUtil.releaseLock(lockKey, requestId);
    }
  }

  @Override
  @Transactional
  public VenueReservation occupyCourt(Integer reservationId, Integer userId) {
    // 检查限流 - 高并发预约模块专用
    if (!redisRateLimiterUtil.checkReservationRateLimit(userId.toString())) {
      throw new IllegalArgumentException("请求过于频繁，请稍后再试");
    }

    // 检查用户活跃预约数量 - 统计当前用户所有 status = 'active' 的预约/占用记录总数（包括 reservation 和
    // occupation 类型）
    // 只有当活跃记录数量 >= 3 时，才阻止新的预约/占用操作
    // 单次预约中，用户选择 1 个场地，只要当前活跃记录数 < 3，就允许提交
    long activeCount = reservationRepository.countActiveReservationsByUserId(userId);
    if (activeCount >= 3) {
      throw new IllegalArgumentException("您最多只能同时预约3个场地，请先完成或取消后再预约");
    }

    // 获取被占用的预约
    VenueReservation originalReservation = reservationRepository.findById(reservationId).orElse(null);
    if (originalReservation == null) {
      throw new IllegalArgumentException("预约不存在");
    }

    // 检查预约状态
    if (!"active".equals(originalReservation.getStatus())) {
      throw new IllegalArgumentException("只能占用状态为active的预约");
    }

    // 检查是否是自己的预约
    if (originalReservation.getUserId().equals(userId)) {
      throw new IllegalArgumentException("不能占用自己的预约");
    }

    // 检查是否超过30分钟
    LocalDateTime reservationTime = LocalDateTime.of(originalReservation.getReserveDate(),
        originalReservation.getStartTime());
    LocalDateTime now = LocalDateTime.now();
    if (now.isBefore(reservationTime.plusMinutes(30))) {
      throw new IllegalArgumentException("预约开始30分钟后才能占用");
    }

    // 生成分布式锁键 - 高并发预约模块专用
    String lockKey = redisDistributedLockUtil.generateLockKey(
        "sports:court",
        originalReservation.getCourtId().toString(),
        originalReservation.getReserveDate().toString() + "-" + originalReservation.getStartTime().toString());
    String requestId = UUID.randomUUID().toString();
    int expireTime = 30; // 锁过期时间30秒

    try {
      // 获取分布式锁 - 高并发预约模块专用
      if (!redisDistributedLockUtil.acquireLock(lockKey, requestId, expireTime)) {
        throw new IllegalArgumentException("系统繁忙，请稍后再试");
      }

      // 检查场地状态
      Court court = courtRepository.findById(originalReservation.getCourtId()).orElse(null);
      if (court == null || !"reserved".equals(court.getStatus())) {
        throw new IllegalArgumentException("场地状态不正确");
      }

      // 更新原预约状态
      originalReservation.setStatus("replaced");
      originalReservation.setActualEndTime(now);
      originalReservation.setActualDuration((int) (now.getHour() * 60 + now.getMinute() -
          reservationTime.getHour() * 60 - reservationTime.getMinute()));
      reservationRepository.save(originalReservation);

      // 创建新的占用预约
      VenueReservation occupation = new VenueReservation();
      occupation.setUserId(userId);
      occupation.setCourtId(originalReservation.getCourtId());
      occupation.setVenueId(originalReservation.getVenueId());
      occupation.setReserveDate(originalReservation.getReserveDate());
      occupation.setStartTime(now.toLocalTime());
      occupation.setDuration(
          (int) (originalReservation.getEndTime().getHour() * 60 + originalReservation.getEndTime().getMinute() -
              now.getHour() * 60 - now.getMinute()));
      occupation.setEndTime(originalReservation.getEndTime());
      occupation.setType("occupation");
      occupation.setStatus("active");
      occupation.setCreatedAt(now);

      // 保存占用预约
      VenueReservation savedOccupation = reservationRepository.save(occupation);

      // 更新场地状态
      courtService.updateCourtStatus(originalReservation.getCourtId(), "occupied");

      // 清除缓存 - 高并发预约模块专用
      redisCacheUtil.clearVenueReservationCache(originalReservation.getCourtId().longValue(),
          originalReservation.getReserveDate().toString());

      return savedOccupation;
    } catch (ObjectOptimisticLockingFailureException e) {
      throw new IllegalArgumentException("操作失败：该场地信息已被其他用户修改，请刷新页面后重试");
    } finally {
      // 释放分布式锁 - 高并发预约模块专用
      redisDistributedLockUtil.releaseLock(lockKey, requestId);
    }
  }

  @Override
  @Transactional
  public void leaveCourt(Integer reservationId, Integer userId) {
    try {
      // 获取预约
      VenueReservation reservation = reservationRepository.findById(reservationId).orElse(null);
      if (reservation == null) {
        throw new IllegalArgumentException("预约不存在");
      }

      // 检查是否是预约人本人
      if (!reservation.getUserId().equals(userId)) {
        throw new IllegalArgumentException("只能离开自己的预约");
      }

      // 检查预约状态
      if (!"active".equals(reservation.getStatus())) {
        throw new IllegalArgumentException("只能离开状态为active的预约");
      }

      // 更新预约状态
      reservation.setStatus("cancelled");
      reservation.setActualEndTime(LocalDateTime.now());
      reservation.setActualDuration(
          (int) (reservation.getActualEndTime().getHour() * 60 + reservation.getActualEndTime().getMinute() -
              reservation.getStartTime().getHour() * 60 - reservation.getStartTime().getMinute()));
      reservationRepository.save(reservation);

      // 更新场地状态
      courtService.updateCourtStatus(reservation.getCourtId(), "available");

      // 清除缓存 - 高并发预约模块专用
      redisCacheUtil.clearVenueReservationCache(reservation.getCourtId().longValue(),
          reservation.getReserveDate().toString());
    } catch (ObjectOptimisticLockingFailureException e) {
      throw new IllegalArgumentException("操作失败：该场地信息已被其他用户修改，请刷新页面后重试");
    }
  }

    @Override
    public List<VenueReservation> getReservationsByUserId(Integer userId) {
        return reservationRepository.findByUserId(userId);
    }

    /**
     * ✅ 批量查询优化版 - 获取场地的所有预约
     */
    @Override
    public List<VenueReservation> getReservationsByCourtId(Integer courtId, Integer currentUserId) {
        // 获取该场地所有活跃的预约
        List<VenueReservation> reservations = reservationRepository.findByCourtIdAndStatus(courtId, "active");

        if (reservations.isEmpty()) {
            return reservations;
        }

        // 批量获取所有预约的用户ID（去重）
        List<Integer> userIds = reservations.stream()
                .map(VenueReservation::getUserId)
                .distinct()
                .collect(Collectors.toList());

        // 批量查询用户信息
        Map<Integer, User> userMap = userRepository.findAllById(userIds)
                .stream()
                .collect(Collectors.toMap(User::getId, user -> user));

        // 填充数据
        for (VenueReservation reservation : reservations) {
            Integer userId = reservation.getUserId();
            User user = userMap.get(userId);

            // 设置用户名
            if (user != null && user.getUsername() != null) {
                reservation.setUserName(user.getUsername());
            } else {
                reservation.setUserName("用户" + userId);
            }

            // 设置是否是当前用户
            reservation.setIsOwner(currentUserId != null && currentUserId.equals(userId));
        }

        return reservations;
    }

    @Override
    public VenueReservation getReservationById(Integer id) {
        return reservationRepository.findById(id).orElse(null);
    }

    @Override
  @Transactional
  public void processExpiredReservations() {
    List<VenueReservation> activeReservations = reservationRepository.findByStatus("active");
    LocalDateTime now = LocalDateTime.now();

    for (VenueReservation reservation : activeReservations) {
      LocalDateTime endTime = LocalDateTime.of(reservation.getReserveDate(), reservation.getEndTime());
      if (now.isAfter(endTime)) {
        // 更新预约状态
        reservation.setStatus("completed");
        reservation.setActualEndTime(now);
        reservation.setActualDuration((int) (now.getHour() * 60 + now.getMinute() -
            reservation.getStartTime().getHour() * 60 - reservation.getStartTime().getMinute()));
        reservationRepository.save(reservation);

        // 更新场地状态
        courtService.updateCourtStatus(reservation.getCourtId(), "available");
      }
    }
  }

    public UserRepository getUserRepository() {
        return userRepository;
    }
}