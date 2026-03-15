package com.campus.forum.sports.service;

import com.campus.forum.sports.entity.Court;
import java.util.List;

public interface CourtService {
  List<Court> getCourtsByVenueId(Integer venueId);
  Court getCourtById(Integer id);
  void updateCourtStatus(Integer courtId, String status);
  Court getCourtByIdAndVenueId(Integer id, Integer venueId);
}