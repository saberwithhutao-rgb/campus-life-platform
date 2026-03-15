package com.campus.forum.sports.controller;

import com.campus.forum.common.Result;
import com.campus.forum.common.annotation.CurrentUser;
import com.campus.forum.sports.dto.VenueReservationDTO;
import com.campus.forum.sports.service.VenueReservationService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/sports/reservations")
public class VenueReservationController {
  private final VenueReservationService reservationService;

  public VenueReservationController(VenueReservationService reservationService) {
    this.reservationService = reservationService;
  }

  @PostMapping
  public Result<?> createReservation(@RequestBody VenueReservationDTO reservationDTO, @CurrentUser Integer userId) {
    try {
      reservationDTO.setUserId(userId);
      return Result.success(reservationService.createReservation(reservationDTO));
    } catch (Exception e) {
      e.printStackTrace();
      return Result.fail(500, "创建预约失败：" + e.getMessage());
    }
  }

  @PostMapping("/{id}/occupy")
  public Result<?> occupyCourt(@PathVariable Integer id, @CurrentUser Integer userId) {
    try {
      return Result.success(reservationService.occupyCourt(id, userId));
    } catch (Exception e) {
      e.printStackTrace();
      return Result.fail(500, "占用场地失败：" + e.getMessage());
    }
  }

  @PostMapping("/{id}/leave")
  public Result<?> leaveCourt(@PathVariable Integer id, @CurrentUser Integer userId) {
    try {
      reservationService.leaveCourt(id, userId);
      return Result.success(null);
    } catch (Exception e) {
      e.printStackTrace();
      return Result.fail(500, "离开场地失败：" + e.getMessage());
    }
  }

  @GetMapping("/user")
  public Result<?> getReservationsByUserId(@CurrentUser Integer userId) {
    try {
      return Result.success(reservationService.getReservationsByUserId(userId));
    } catch (Exception e) {
      e.printStackTrace();
      return Result.fail(500, "获取预约记录失败：" + e.getMessage());
    }
  }

  @GetMapping("/court/{courtId}")
  public Result<?> getReservationsByCourtId(@PathVariable Integer courtId) {
    try {
      return Result.success(reservationService.getReservationsByCourtId(courtId));
    } catch (Exception e) {
      e.printStackTrace();
      return Result.fail(500, "获取场地预约失败：" + e.getMessage());
    }
  }
}