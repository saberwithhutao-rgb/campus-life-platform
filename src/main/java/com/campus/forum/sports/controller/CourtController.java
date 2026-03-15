package com.campus.forum.sports.controller;

import com.campus.forum.common.Result;
import com.campus.forum.sports.service.CourtService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/sports/courts")
public class CourtController {
  private final CourtService courtService;

  public CourtController(CourtService courtService) {
    this.courtService = courtService;
  }

  @GetMapping("/venue/{venueId}")
  public Result<?> getCourtsByVenueId(@PathVariable Integer venueId) {
    try {
      return Result.success(courtService.getCourtsByVenueId(venueId));
    } catch (Exception e) {
      e.printStackTrace();
      return Result.fail(500, "获取场地列表失败：" + e.getMessage());
    }
  }
}