package com.campus.forum.sports.repository;

import com.campus.forum.sports.entity.Court;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CourtRepository extends JpaRepository<Court, Integer> {
  List<Court> findByVenueId(Integer venueId);
  Court findByIdAndVenueId(Integer id, Integer venueId);
}