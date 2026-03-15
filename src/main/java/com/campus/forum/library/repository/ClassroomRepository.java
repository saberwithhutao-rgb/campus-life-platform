package com.campus.forum.library.repository;

import com.campus.forum.library.entity.Classroom;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ClassroomRepository extends JpaRepository<Classroom, Integer> {
  List<Classroom> findByFloorId(Integer floorId);
}
