package com.campus.forum.sports.service;

import com.campus.forum.sports.entity.Venue;
import java.util.List;

public interface VenueService {
  List<Venue> getAllVenues();
  Venue getVenueById(Integer id);
  int getAvailableCourtsCount(Integer venueId);
  int getTotalCourtsCount(Integer venueId);
  int getUsedCourtsCount(Integer venueId);
}