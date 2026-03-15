package com.campus.forum.sports.repository;

import com.campus.forum.sports.entity.Venue;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VenueRepository extends JpaRepository<Venue, Integer> {
}