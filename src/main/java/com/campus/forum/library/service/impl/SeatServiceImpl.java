package com.campus.forum.library.service.impl;

import com.campus.forum.library.entity.Seat;
import com.campus.forum.library.repository.SeatRepository;
import com.campus.forum.library.service.SeatService;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class SeatServiceImpl implements SeatService {
  private final SeatRepository seatRepository;

  public SeatServiceImpl(SeatRepository seatRepository) {
    this.seatRepository = seatRepository;
  }

  @Override
  public List<Seat> getSeatsByClassroomId(Integer classroomId) {
    return seatRepository.findByClassroomId(classroomId);
  }

  @Override
  public void updateSeatStatus(Integer seatId, String status) {
    Seat seat = seatRepository.findById(seatId).orElseThrow(() -> new RuntimeException("座位不存在"));
    seat.setStatus(status);
    seatRepository.save(seat);
  }

  @Override
  public String getSeatStatus(Integer seatId) {
    Seat seat = seatRepository.findById(seatId).orElseThrow(() -> new RuntimeException("座位不存在"));
    return seat.getStatus();
  }
}
