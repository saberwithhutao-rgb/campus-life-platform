package com.campus.forum.library.service;

import com.campus.forum.library.entity.Classroom;
import com.campus.forum.library.dto.AvailableSeatsDTO;
import java.util.List;

public interface ClassroomService {
  List<Classroom> getClassroomsByFloorId(Integer floorId);

  AvailableSeatsDTO getAvailableSeatsByClassroomId(Integer classroomId);

}
