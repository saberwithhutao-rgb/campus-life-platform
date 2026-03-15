package com.campus.forum.library.service;

import com.campus.forum.library.entity.Seat;
import java.util.List;

public interface SeatService {
  List<Seat> getSeatsByClassroomId(Integer classroomId);

  void updateSeatStatus(Integer seatId, String status);

  String getSeatStatus(Integer seatId);
}
