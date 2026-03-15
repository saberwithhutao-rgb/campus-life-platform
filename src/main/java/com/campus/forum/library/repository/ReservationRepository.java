package com.campus.forum.library.repository;

import com.campus.forum.library.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Integer> {

  /**
   * 查询指定座位在指定时间段内的有效预约
   */
  @Query("SELECT r FROM Reservation r WHERE r.seatId = ?1 AND r.reserveDate = ?2 AND r.status = 'active' AND (r.startTime < ?4 AND r.endTime > ?3)")
  List<Reservation> findConflictingReservations(Integer seatId, LocalDate reserveDate, LocalTime startTime,
      LocalTime endTime);

  /**
   * 查询需要自动结束的预约
   */
  @Query("SELECT r FROM Reservation r WHERE r.status = 'active' AND (r.reserveDate < CURRENT_DATE OR (r.reserveDate = CURRENT_DATE AND r.endTime < CURRENT_TIME))")
  List<Reservation> findExpiredReservations();

  /**
   * 根据用户ID查询预约记录
   */
  List<Reservation> findByUserId(Integer userId);

  /**
   * 根据座位ID和状态查询预约
   */
  List<Reservation> findBySeatIdAndStatus(Integer seatId, String status);

  /**
   * 根据用户ID和状态查询预约
   */
  List<Reservation> findByUserIdAndStatus(Integer userId, String status);

  /**
   * 统计指定教室在指定日期和当前时间正在使用中的预约座位数量
   */
  @Query("SELECT COUNT(DISTINCT r.seatId) FROM Reservation r WHERE r.classroomId = ?1 AND r.reserveDate = ?2 AND r.status = ?3 AND r.startTime <= ?4 AND r.endTime > ?4")
  int countDistinctSeatByClassroomIdAndReserveDateAndStatusAndCurrentTime(
      Integer classroomId, LocalDate reserveDate,
      String status, LocalTime currentTime);

  /**
   * 统计用户活跃预约数量
   */
  @Query("SELECT COUNT(*) FROM Reservation r WHERE r.userId = ?1 AND r.status = 'active'")
  int countActiveReservationsByUserId(Integer userId);
}
