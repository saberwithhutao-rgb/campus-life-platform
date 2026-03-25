package com.campus.forum.sports.service.impl;

import com.campus.forum.sports.entity.Venue;
import com.campus.forum.sports.entity.Court;
import com.campus.forum.sports.repository.VenueRepository;
import com.campus.forum.sports.repository.CourtRepository;
import com.campus.forum.sports.service.VenueService;
import com.campus.forum.common.util.RedisCacheUtil;
import org.springframework.stereotype.Service;
import jakarta.annotation.Resource;
import java.util.List;

@Service
public class VenueServiceImpl implements VenueService {
  private final VenueRepository venueRepository;
  private final CourtRepository courtRepository;
  
  @Resource
  private RedisCacheUtil redisCacheUtil;

  public VenueServiceImpl(VenueRepository venueRepository, CourtRepository courtRepository) {
    this.venueRepository = venueRepository;
    this.courtRepository = courtRepository;
  }

  @Override
  public List<Venue> getAllVenues() {
    String cacheKey = "sports:venues:all";
    List<Venue> venues = redisCacheUtil.getCache(cacheKey);
    if (venues == null) {
      venues = venueRepository.findAll();
      redisCacheUtil.setCache(cacheKey, venues, 3600);
    }
    return venues;
  }

  @Override
  public Venue getVenueById(Integer id) {
    return venueRepository.findById(id).orElse(null);
  }

  @Override
  public int getAvailableCourtsCount(Integer venueId) {
    List<Court> courts = courtRepository.findByVenueId(venueId);
    int count = 0;
    for (Court court : courts) {
      if ("available".equals(court.getStatus())) {
        count++;
      }
    }
    return count;
  }

  @Override
  public int getTotalCourtsCount(Integer venueId) {
    Venue venue = venueRepository.findById(venueId).orElse(null);
    return venue != null ? venue.getTotalCourts() : 0;
  }

  @Override
  public int getUsedCourtsCount(Integer venueId) {
    List<Court> courts = courtRepository.findByVenueId(venueId);
    int count = 0;
    for (Court court : courts) {
      if (!"available".equals(court.getStatus())) {
        count++;
      }
    }
    return count;
  }
}