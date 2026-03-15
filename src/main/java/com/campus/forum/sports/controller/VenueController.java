package com.campus.forum.sports.controller;

import com.campus.forum.common.Result;
import com.campus.forum.sports.service.VenueService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/sports/venues")
public class VenueController {
  private final VenueService venueService;

  public VenueController(VenueService venueService) {
    this.venueService = venueService;
  }

  @GetMapping
  public Result<?> getAllVenues() {
    try {
      return Result.success(venueService.getAllVenues());
    } catch (Exception e) {
      e.printStackTrace();
      return Result.fail(500, "获取场馆列表失败：" + e.getMessage());
    }
  }

  @GetMapping("/{venueId}/available-courts")
  public Result<?> getAvailableCourts(@PathVariable Integer venueId) {
    try {
      int totalCourts = venueService.getTotalCourtsCount(venueId);
      int usedCourts = venueService.getUsedCourtsCount(venueId);
      int availableCourts = venueService.getAvailableCourtsCount(venueId);
      
      Map<String, Integer> result = new HashMap<>();
      result.put("totalCourts", totalCourts);
      result.put("usedCourts", usedCourts);
      result.put("availableCourts", availableCourts);
      
      return Result.success(result);
    } catch (Exception e) {
      e.printStackTrace();
      return Result.fail(500, "获取可用场地数失败：" + e.getMessage());
    }
  }
}