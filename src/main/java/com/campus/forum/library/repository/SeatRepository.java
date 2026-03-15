package com.campus.forum.library.repository;

import com.campus.forum.library.entity.Seat;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SeatRepository extends JpaRepository<Seat, Integer> {
  List<Seat> findByClassroomId(Integer classroomId);
}
