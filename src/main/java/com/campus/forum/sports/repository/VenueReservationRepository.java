package com.campus.forum.sports.repository;

import com.campus.forum.sports.entity.VenueReservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.time.LocalDate;
import java.time.LocalTime;

public interface VenueReservationRepository extends JpaRepository<VenueReservation, Integer> {
  List<VenueReservation> findByUserId(Integer userId);

  List<VenueReservation> findByCourtId(Integer courtId);

  List<VenueReservation> findByCourtIdAndStatus(Integer courtId, String status);

  List<VenueReservation> findByStatus(String status);

  @Query("SELECT r FROM VenueReservation r WHERE r.courtId = :courtId AND r.reserveDate = :reserveDate AND r.status = 'active' AND r.startTime < :endTime AND r.endTime > :startTime")
  List<VenueReservation> findOverlappingReservations(
      @Param("courtId") Integer courtId,
      @Param("reserveDate") LocalDate reserveDate,
      @Param("startTime") LocalTime startTime,
      @Param("endTime") LocalTime endTime);

  @Query("SELECT r FROM VenueReservation r WHERE r.courtId = :courtId AND r.reserveDate = :reserveDate AND r.status = 'active'")
  List<VenueReservation> findActiveReservationsByCourtAndDate(
      @Param("courtId") Integer courtId,
      @Param("reserveDate") LocalDate reserveDate);

  @Query("SELECT COUNT(r) FROM VenueReservation r WHERE r.userId = :userId AND r.status = 'active'")
  long countActiveReservationsByUserId(@Param("userId") Integer userId);
}