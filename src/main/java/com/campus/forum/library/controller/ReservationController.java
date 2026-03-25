package com.campus.forum.library.controller;

import com.campus.forum.common.Result;
import com.campus.forum.library.entity.Reservation;
import com.campus.forum.library.dto.ReservationDTO;
import com.campus.forum.library.service.ReservationService;
import com.campus.forum.util.JwtUtil;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/library/reservations")
public class ReservationController {
  private final ReservationService reservationService;
  private final JwtUtil jwtUtil;

  public ReservationController(ReservationService reservationService, JwtUtil jwtUtil) {
    this.reservationService = reservationService;
    this.jwtUtil = jwtUtil;
  }

  @Async("virtualThreadPool")
  @PostMapping
  public CompletableFuture<Result<Reservation>> createReservation(
      @RequestBody ReservationDTO reservationDTO,
      @RequestHeader(value = "Authorization") String authorization) {
    try {
      Integer userId = getUserIdFromToken(authorization);
      Reservation reservation = reservationService.createReservation(reservationDTO, userId);
      return CompletableFuture.completedFuture(Result.success(reservation));
    } catch (IllegalArgumentException e) {
      return CompletableFuture.completedFuture(Result.fail(401, e.getMessage()));
    } catch (Exception e) {
      e.printStackTrace();
      return CompletableFuture.completedFuture(Result.fail(500, "创建预约失败：" + e.getMessage()));
    }
  }

  @Async("virtualThreadPool")
  @PostMapping("/{id}/occupy")
  public CompletableFuture<Result<Reservation>> occupySeat(
      @PathVariable Integer id,
      @RequestHeader(value = "Authorization") String authorization) {
    try {
      Integer userId = getUserIdFromToken(authorization);
      Reservation reservation = reservationService.occupySeat(id, userId);
      return CompletableFuture.completedFuture(Result.success(reservation));
    } catch (IllegalArgumentException e) {
      return CompletableFuture.completedFuture(Result.fail(401, e.getMessage()));
    } catch (Exception e) {
      e.printStackTrace();
      return CompletableFuture.completedFuture(Result.fail(500, "占用座位失败：" + e.getMessage()));
    }
  }

  @Async("virtualThreadPool")
  @PostMapping("/{id}/leave")
  public CompletableFuture<Result<?>> leaveSeat(
      @PathVariable Integer id,
      @RequestHeader(value = "Authorization") String authorization) {
    try {
      Integer userId = getUserIdFromToken(authorization);
      reservationService.leaveSeat(id, userId);
      return CompletableFuture.completedFuture(Result.success(null));
    } catch (IllegalArgumentException e) {
      return CompletableFuture.completedFuture(Result.fail(401, e.getMessage()));
    } catch (Exception e) {
      e.printStackTrace();
      return CompletableFuture.completedFuture(Result.fail(500, "离开座位失败：" + e.getMessage()));
    }
  }

  @Async("virtualThreadPool")
  @GetMapping("/user")
  public CompletableFuture<Result<List<Reservation>>> getCurrentUserReservations(
      @RequestHeader(value = "Authorization") String authorization) {
    try {
      Integer userId = getUserIdFromToken(authorization);
      List<Reservation> reservations = reservationService.getReservationsByUserId(userId);
      return CompletableFuture.completedFuture(Result.success(reservations));
    } catch (IllegalArgumentException e) {
      return CompletableFuture.completedFuture(Result.fail(401, e.getMessage()));
    } catch (Exception e) {
      e.printStackTrace();
      return CompletableFuture.completedFuture(Result.fail(500, "获取预约记录失败：" + e.getMessage()));
    }
  }

  @Async("virtualThreadPool")
  @GetMapping("/seat/{seatId}")
  public CompletableFuture<Result<List<Reservation>>> getReservationBySeatId(@PathVariable Integer seatId) {
    try {
      List<Reservation> reservations = reservationService.getReservationBySeatId(seatId);
      return CompletableFuture.completedFuture(Result.success(reservations));
    } catch (Exception e) {
      e.printStackTrace();
      return CompletableFuture.completedFuture(Result.fail(500, "获取座位预约失败：" + e.getMessage()));
    }
  }

  private Integer getUserIdFromToken(String authorization) {
    return jwtUtil.getUserIdFromToken(jwtUtil.extractToken(authorization));
  }
}