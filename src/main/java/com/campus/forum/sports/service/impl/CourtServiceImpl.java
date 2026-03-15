package com.campus.forum.sports.service.impl;

import com.campus.forum.sports.entity.Court;
import com.campus.forum.sports.repository.CourtRepository;
import com.campus.forum.sports.service.CourtService;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class CourtServiceImpl implements CourtService {
  private final CourtRepository courtRepository;

  public CourtServiceImpl(CourtRepository courtRepository) {
    this.courtRepository = courtRepository;
  }

  @Override
  public List<Court> getCourtsByVenueId(Integer venueId) {
    return courtRepository.findByVenueId(venueId);
  }

  @Override
  public Court getCourtById(Integer id) {
    return courtRepository.findById(id).orElse(null);
  }

  @Override
  public void updateCourtStatus(Integer courtId, String status) {
    Court court = courtRepository.findById(courtId).orElse(null);
    if (court != null) {
      court.setStatus(status);
      courtRepository.save(court);
    }
  }

  @Override
  public Court getCourtByIdAndVenueId(Integer id, Integer venueId) {
    return courtRepository.findByIdAndVenueId(id, venueId);
  }
}