package com.campus.forum.library.controller;

import com.campus.forum.common.Result;
import com.campus.forum.library.entity.Classroom;
import com.campus.forum.library.service.ClassroomService;
import com.campus.forum.library.dto.AvailableSeatsDTO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/api/library/classrooms")
public class ClassroomController {
  private final ClassroomService classroomService;

  public ClassroomController(ClassroomService classroomService) {
    this.classroomService = classroomService;
  }

  @GetMapping("/floor/{floorId}")
  public Result<List<Classroom>> getClassroomsByFloorId(@PathVariable Integer floorId) {
    try {
      List<Classroom> classrooms = classroomService.getClassroomsByFloorId(floorId);
      return Result.success(classrooms);
    } catch (Exception e) {
      e.printStackTrace();
      return Result.fail(500, "获取教室列表失败：" + e.getMessage());
    }
  }

  @GetMapping("/{classroomId}/available-seats")
  public Result<AvailableSeatsDTO> getAvailableSeatsByClassroomId(@PathVariable Integer classroomId) {
    try {
      AvailableSeatsDTO availableSeats = classroomService.getAvailableSeatsByClassroomId(classroomId);
      return Result.success(availableSeats);
    } catch (Exception e) {
      e.printStackTrace();
      return Result.fail(500, "获取可用座位数失败：" + e.getMessage());
    }
  }
}