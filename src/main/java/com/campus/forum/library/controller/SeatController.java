package com.campus.forum.library.controller;

import com.campus.forum.common.Result;
import com.campus.forum.library.entity.Seat;
import com.campus.forum.library.service.SeatService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/api/library/seats")
public class SeatController {
  private final SeatService seatService;

  public SeatController(SeatService seatService) {
    this.seatService = seatService;
  }

  @GetMapping("/classroom/{classroomId}")
  public Result<List<Seat>> getSeatsByClassroomId(@PathVariable Integer classroomId) {
    try {
      List<Seat> seats = seatService.getSeatsByClassroomId(classroomId);
      return Result.success(seats);
    } catch (Exception e) {
      e.printStackTrace();
      return Result.fail(500, "获取座位列表失败：" + e.getMessage());
    }
  }
}
