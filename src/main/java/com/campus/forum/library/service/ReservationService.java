package com.campus.forum.library.service;

import com.campus.forum.library.entity.Reservation;
import com.campus.forum.library.dto.ReservationDTO;
import java.util.List;

public interface ReservationService {
  Reservation createReservation(ReservationDTO reservationDTO, Integer userId);

  Reservation occupySeat(Integer reservationId, Integer userId);

  void leaveSeat(Integer reservationId, Integer userId);

  List<Reservation> getReservationsByUserId(Integer userId);

  List<Reservation> getReservationBySeatId(Integer seatId);

  void processExpiredReservations();
}