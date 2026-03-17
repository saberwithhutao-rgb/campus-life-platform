package com.campus.forum.library.controller;

import com.campus.forum.common.Result;
import com.campus.forum.common.annotation.CurrentUser;
import com.campus.forum.library.entity.Reservation;
import com.campus.forum.library.dto.ReservationDTO;
import com.campus.forum.library.service.ReservationService;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/library/reservations")
public class ReservationController {
  private final ReservationService reservationService;

  public ReservationController(ReservationService reservationService) {
    this.reservationService = reservationService;
  }

  @PostMapping
  public Result<Reservation> createReservation(@RequestBody ReservationDTO reservationDTO,
      @CurrentUser Integer userId) {
    try {
      reservationDTO.setUserId(userId);

      String startTime = String.valueOf(reservationDTO.getStartTime());
      Integer duration = reservationDTO.getDuration();

      String[] timeParts = startTime.split(":");
      int hours = Integer.parseInt(timeParts[0]);
      int minutes = Integer.parseInt(timeParts[1]);

      int totalMinutes = hours * 60 + minutes + duration;
      int endHours = totalMinutes / 60;
      int endMinutes = totalMinutes % 60;

      if (endHours >= 24) {
          return Result.fail(400, "预约时间已超过当天24点，请重新选择开始时间或减少时长");
      }

      Reservation reservation = reservationService.createReservation(reservationDTO);
      return Result.success(reservation);
    } catch (Exception e) {
      e.printStackTrace();
      return Result.fail(500, "创建预约失败：" + e.getMessage());

    }
  }

  @PostMapping("/{id}/occupy")
  public Result<Reservation> occupySeat(@PathVariable Integer id, @CurrentUser Integer userId) {
    try {
      Reservation reservation = reservationService.occupySeat(id, userId);
      return Result.success(reservation);
    } catch (Exception e) {
      e.printStackTrace();
      return Result.fail(500, "占用座位失败：" + e.getMessage());
    }
  }

  @PostMapping("/{id}/leave")
  public Result<?> leaveSeat(@PathVariable Integer id, @CurrentUser Integer userId) {
    try {
      reservationService.leaveSeat(id, userId);
      return Result.success(null);
    } catch (Exception e) {
      e.printStackTrace();
      return Result.fail(500, "离开座位失败：" + e.getMessage());
    }
  }

  @GetMapping("/user")
  public Result<List<Reservation>> getReservationsByUserId(@CurrentUser Integer userId) {
    try {
      List<Reservation> reservations = reservationService.getReservationsByUserId(userId);
      return Result.success(reservations);
    } catch (Exception e) {
      e.printStackTrace();
      return Result.fail(500, "获取预约记录失败：" + e.getMessage());
    }
  }

  @GetMapping("/seat/{seatId}")
  public Result<List<Reservation>> getReservationBySeatId(@PathVariable Integer seatId) {
    try {
      List<Reservation> reservations = reservationService.getReservationBySeatId(seatId);
      return Result.success(reservations);
    } catch (Exception e) {
      e.printStackTrace();
      return Result.fail(500, "获取座位预约失败：" + e.getMessage());
    }
  }
}