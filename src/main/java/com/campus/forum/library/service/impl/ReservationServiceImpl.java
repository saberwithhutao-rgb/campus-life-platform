package com.campus.forum.library.service.impl;

import com.campus.forum.library.entity.Reservation;
import com.campus.forum.library.dto.ReservationDTO;
import com.campus.forum.library.repository.ReservationRepository;
import com.campus.forum.library.service.ReservationService;
import com.campus.forum.library.service.SeatService;
import com.campus.forum.library.util.TimeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ReservationServiceImpl implements ReservationService {
  private static final Logger log = LoggerFactory.getLogger(ReservationServiceImpl.class);
  private final ReservationRepository reservationRepository;
  private final SeatService seatService;

  public ReservationServiceImpl(ReservationRepository reservationRepository, SeatService seatService) {
    this.reservationRepository = reservationRepository;
    this.seatService = seatService;
  }

  /**
   * 检查用户是否有有效预约/占用
   */
  private void checkUserHasActiveReservation(Integer userId) {
    List<Reservation> activeList = reservationRepository.findByUserIdAndStatus(userId, "active");
    if (!activeList.isEmpty()) {
      throw new RuntimeException("您已有正在进行的预约或占用，无法同时操作多个座位");
    }
  }

  /**
   * 检查用户活跃预约数量是否超过限制
   */
  private void checkUserActiveReservationLimit(Integer userId) {
    int activeCount = reservationRepository.countActiveReservationsByUserId(userId);
    if (activeCount >= 3) {
      throw new RuntimeException("您最多只能同时预约/占用3个座位，请完成现有预约后再操作");
    }
  }

  @Override
  @Transactional(isolation = Isolation.SERIALIZABLE)
  public Reservation createReservation(ReservationDTO reservationDTO) {
    // 检查用户活跃预约数量是否超过限制
    checkUserActiveReservationLimit(reservationDTO.getUserId());

    // 检查预约时间是否合法
    if (!TimeUtils.isReservationTimeValid(reservationDTO.getReserveDate(), reservationDTO.getStartTime())) {
      throw new RuntimeException("不能预约已过去的时间段");
    }

    // 检查是否存在冲突预约
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
    reservation.setUserId(reservationDTO.getUserId());
    reservation.setSeatId(reservationDTO.getSeatId());
    reservation.setClassroomId(reservationDTO.getClassroomId());
    reservation.setReserveDate(reservationDTO.getReserveDate());
    reservation.setStartTime(reservationDTO.getStartTime());
    reservation.setDuration(reservationDTO.getDuration());
    reservation.setEndTime(TimeUtils.calculateEndTime(reservationDTO.getStartTime(), reservationDTO.getDuration()));
    reservation.setType("reservation");
    reservation.setStatus("active");
    reservation.setCreatedAt(LocalDateTime.now());

    // 保存预约
    Reservation savedReservation = reservationRepository.save(reservation);

    // 更新座位状态为reserved
    seatService.updateSeatStatus(reservationDTO.getSeatId(), "reserved");

    return savedReservation;
  }

  @Override
  @Transactional
  public Reservation occupySeat(Integer reservationId, Integer userId) {
    // 检查用户活跃预约数量是否超过限制
    checkUserActiveReservationLimit(userId);
    
    Reservation reservation = reservationRepository.findById(reservationId)
        .orElseThrow(() -> new RuntimeException("预约不存在"));

    // 检查是否可以占用
    if (!reservation.getStatus().equals("active")) {
      throw new RuntimeException("该预约已被处理");
    }

    // 检查操作者是否是原预约人
    if (reservation.getUserId().equals(userId)) {
      throw new RuntimeException("不能占用自己的预约");
    }

    // 检查是否满足30分钟条件
    if (!TimeUtils.isAfter30MinutesFromStart(reservation.getReserveDate(), reservation.getStartTime())) {
      throw new RuntimeException("预约开始时间后30分钟才能占用");
    }

    // 检查座位状态
    Integer seatId = reservation.getSeatId();
    String seatStatus = seatService.getSeatStatus(seatId);

    // 打印关键日志
    log.info("占用前检查：预约ID={}, 预约状态={}, 座位ID={}, 座位状态={}",
        reservationId, reservation.getStatus(), seatId, seatStatus);

    if (!seatStatus.equals("reserved")) {
      throw new RuntimeException("座位状态不正确，当前状态为：" + seatStatus + "，仅 reserved 状态可被占用");
    }

    // 创建新的占用记录
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

    // 保存占用记录
    Reservation savedOccupation = reservationRepository.save(occupation);

    // 更新原预约记录为replaced
    reservation.setStatus("replaced");
    reservation.setActualEndTime(LocalDateTime.now());
    reservationRepository.save(reservation);

    // 更新座位状态为occupied
    seatService.updateSeatStatus(reservation.getSeatId(), "occupied");

    return savedOccupation;
  }

  @Override
  @Transactional
  public void leaveSeat(Integer reservationId, Integer userId) {
    Reservation reservation = reservationRepository.findById(reservationId)
        .orElseThrow(() -> new RuntimeException("预约不存在"));

    // 检查是否是本人操作
    if (!reservation.getUserId().equals(userId)) {
      throw new RuntimeException("只能离开自己的预约");
    }

    // 检查是否可以离开
    if (!reservation.getStatus().equals("active")) {
      throw new RuntimeException("该预约已结束或已取消");
    }

    // 更新预约状态
    reservation.setStatus("cancelled");
    reservation.setActualEndTime(LocalDateTime.now());
    // 计算实际使用时长
    long actualDuration = java.time.Duration.between(reservation.getCreatedAt(), LocalDateTime.now()).toMinutes();
    reservation.setActualDurationMinutes((int) actualDuration);
    reservationRepository.save(reservation);

    // 恢复座位状态为available
    seatService.updateSeatStatus(reservation.getSeatId(), "available");
  }

  @Override
  public List<Reservation> getReservationsByUserId(Integer userId) {
    return reservationRepository.findByUserId(userId);
  }

  @Override
  public List<Reservation> getReservationBySeatId(Integer seatId) {
    return reservationRepository.findBySeatIdAndStatus(seatId, "active");
  }

  @Override
  @Scheduled(cron = "0 * * * * *") // 每分钟执行一次
  @Transactional
  public void processExpiredReservations() {
    // 查询所有状态为 active 且结束时间 < 当前时间 的预约
    List<Reservation> expiredReservations = reservationRepository.findExpiredReservations();
    for (Reservation reservation : expiredReservations) {
      reservation.setStatus("completed");
      reservation.setActualEndTime(LocalDateTime.now());
      reservationRepository.save(reservation);
      // 恢复座位状态
      seatService.updateSeatStatus(reservation.getSeatId(), "available");
    }

    // 打印日志
    if (!expiredReservations.isEmpty()) {
      System.out.println("定时任务：自动释放了 " + expiredReservations.size() + " 条过期预约");
    }
  }
}