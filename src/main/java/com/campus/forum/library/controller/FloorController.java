package com.campus.forum.library.controller;

import com.campus.forum.common.Result;
import com.campus.forum.library.entity.Floor;
import com.campus.forum.library.service.FloorService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/api/library/floors")
public class FloorController {
  private final FloorService floorService;

  public FloorController(FloorService floorService) {
    this.floorService = floorService;
  }

  @GetMapping
  public Result<List<Floor>> getAllFloors() {
    try {
      List<Floor> floors = floorService.getAllFloors();
      return Result.success(floors);
    } catch (Exception e) {
      e.printStackTrace();
      return Result.fail(500, "获取楼层列表失败：" + e.getMessage());
    }
  }
}
