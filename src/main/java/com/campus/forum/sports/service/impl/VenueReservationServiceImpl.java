package com.campus.forum.sports.service.impl;

import com.campus.forum.sports.entity.VenueReservation;
import com.campus.forum.sports.entity.Court;
import com.campus.forum.sports.dto.VenueReservationDTO;
import com.campus.forum.sports.repository.VenueReservationRepository;
import com.campus.forum.sports.repository.CourtRepository;
import com.campus.forum.sports.service.VenueReservationService;
import com.campus.forum.sports.service.CourtService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class VenueReservationServiceImpl implements VenueReservationService {
  private final VenueReservationRepository reservationRepository;
  private final CourtRepository courtRepository;
  private final CourtService courtService;

  public VenueReservationServiceImpl(VenueReservationRepository reservationRepository, CourtRepository courtRepository,
      CourtService courtService) {
    this.reservationRepository = reservationRepository;
    this.courtRepository = courtRepository;
    this.courtService = courtService;
  }

  @Override
  @Transactional
  public VenueReservation createReservation(VenueReservationDTO reservationDTO) {
    // 检查用户活跃预约数量 - 统计当前用户所有 status = 'active' 的预约/占用记录总数（包括 reservation 和
    // occupation 类型）
    // 只有当活跃记录数量 >= 3 时，才阻止新的预约/占用操作
    // 单次预约中，用户选择 1 个场地，只要当前活跃记录数 < 3，就允许提交
    long activeCount = reservationRepository.countActiveReservationsByUserId(reservationDTO.getUserId());
    if (activeCount >= 3) {
      throw new IllegalArgumentException("您最多只能同时预约2个场地，请先完成或取消后再预约");
    }

    // 解析日期和时间
    LocalDate reserveDate = LocalDate.parse(reservationDTO.getReserveDate());
    LocalTime startTime = LocalTime.parse(reservationDTO.getStartTime());
    LocalTime endTime = LocalTime.parse(reservationDTO.getEndTime());

    // 检查时间是否合法
    LocalDateTime now = LocalDateTime.now();
    LocalDateTime reservationTime = LocalDateTime.of(reserveDate, startTime);
    if (reservationTime.isBefore(now)) {
      throw new IllegalArgumentException("预约时间不能早于当前时间");
    }

    // 检查时间重叠
    List<VenueReservation> overlappingReservations = reservationRepository.findOverlappingReservations(
        reservationDTO.getCourtId(), reserveDate, startTime, endTime);
    if (!overlappingReservations.isEmpty()) {
      throw new IllegalArgumentException("该时间段已被预约");
    }

    // 创建预约
    VenueReservation reservation = new VenueReservation();
    reservation.setUserId(reservationDTO.getUserId());
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

    return savedReservation;
  }

  @Override
  @Transactional
  public VenueReservation occupyCourt(Integer reservationId, Integer userId) {
    // 检查用户活跃预约数量 - 统计当前用户所有 status = 'active' 的预约/占用记录总数（包括 reservation 和
    // occupation 类型）
    // 只有当活跃记录数量 >= 3 时，才阻止新的预约/占用操作
    // 单次预约中，用户选择 1 个场地，只要当前活跃记录数 < 3，就允许提交
    long activeCount = reservationRepository.countActiveReservationsByUserId(userId);
    if (activeCount >= 3) {
      throw new IllegalArgumentException("您最多只能同时预约2个场地，请先完成或取消后再预约");
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

    return savedOccupation;
  }

  @Override
  @Transactional
  public void leaveCourt(Integer reservationId, Integer userId) {
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
  }

  @Override
  public List<VenueReservation> getReservationsByUserId(Integer userId) {
    return reservationRepository.findByUserId(userId);
  }

  @Override
  public List<VenueReservation> getReservationsByCourtId(Integer courtId) {
    return reservationRepository.findByCourtIdAndStatus(courtId, "active");
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
}