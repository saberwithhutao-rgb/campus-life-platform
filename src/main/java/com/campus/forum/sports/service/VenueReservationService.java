package com.campus.forum.sports.service;

import com.campus.forum.sports.entity.VenueReservation;
import com.campus.forum.sports.dto.VenueReservationDTO;
import java.util.List;

public interface VenueReservationService {
  VenueReservation createReservation(VenueReservationDTO reservationDTO, Integer userId);
  VenueReservation occupyCourt(Integer reservationId, Integer userId);
  void leaveCourt(Integer reservationId, Integer userId);
  List<VenueReservation> getReservationsByUserId(Integer userId);
  List<VenueReservation> getReservationsByCourtId(Integer courtId);
  VenueReservation getReservationById(Integer id);
  void processExpiredReservations();
}