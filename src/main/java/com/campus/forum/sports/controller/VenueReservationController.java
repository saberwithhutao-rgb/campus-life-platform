package com.campus.forum.sports.controller;

import com.campus.forum.common.Result;
import com.campus.forum.sports.dto.VenueReservationDTO;
import com.campus.forum.sports.service.VenueReservationService;
import com.campus.forum.util.JwtUtil;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/sports/reservations")
public class VenueReservationController {
  private final VenueReservationService reservationService;
  private final JwtUtil jwtUtil;

  public VenueReservationController(VenueReservationService reservationService, JwtUtil jwtUtil) {
    this.reservationService = reservationService;
    this.jwtUtil = jwtUtil;
  }

  @Async("virtualThreadPool")
  @PostMapping
  public CompletableFuture<Result<?>> createReservation(
      @RequestBody VenueReservationDTO reservationDTO,
      @RequestHeader(value = "Authorization") String authorization) {
    try {
      Integer userId = getUserIdFromToken(authorization);
      return CompletableFuture.completedFuture(Result.success(reservationService.createReservation(reservationDTO, userId)));
    } catch (IllegalArgumentException e) {
      return CompletableFuture.completedFuture(Result.fail(401, e.getMessage()));
    } catch (Exception e) {
      e.printStackTrace();
      return CompletableFuture.completedFuture(Result.fail(500, "创建预约失败：" + e.getMessage()));
    }
  }

  @Async("virtualThreadPool")
  @PostMapping("/{id}/occupy")
  public CompletableFuture<Result<?>> occupyCourt(
      @PathVariable Integer id,
      @RequestHeader(value = "Authorization") String authorization) {
    try {
      Integer userId = getUserIdFromToken(authorization);
      return CompletableFuture.completedFuture(Result.success(reservationService.occupyCourt(id, userId)));
    } catch (IllegalArgumentException e) {
      return CompletableFuture.completedFuture(Result.fail(401, e.getMessage()));
    } catch (Exception e) {
      e.printStackTrace();
      return CompletableFuture.completedFuture(Result.fail(500, "占用场地失败：" + e.getMessage()));
    }
  }

  @Async("virtualThreadPool")
  @PostMapping("/{id}/leave")
  public CompletableFuture<Result<?>> leaveCourt(
      @PathVariable Integer id,
      @RequestHeader(value = "Authorization") String authorization) {
    try {
      Integer userId = getUserIdFromToken(authorization);
      reservationService.leaveCourt(id, userId);
      return CompletableFuture.completedFuture(Result.success(null));
    } catch (IllegalArgumentException e) {
      return CompletableFuture.completedFuture(Result.fail(401, e.getMessage()));
    } catch (Exception e) {
      e.printStackTrace();
      return CompletableFuture.completedFuture(Result.fail(500, "离开场地失败：" + e.getMessage()));
    }
  }

  @Async("virtualThreadPool")
  @GetMapping("/user")
  public CompletableFuture<Result<?>> getCurrentUserReservations(
      @RequestHeader(value = "Authorization") String authorization) {
    try {
      Integer userId = getUserIdFromToken(authorization);
      return CompletableFuture.completedFuture(Result.success(reservationService.getReservationsByUserId(userId)));
    } catch (IllegalArgumentException e) {
      return CompletableFuture.completedFuture(Result.fail(401, e.getMessage()));
    } catch (Exception e) {
      e.printStackTrace();
      return CompletableFuture.completedFuture(Result.fail(500, "获取预约记录失败：" + e.getMessage()));
    }
  }

  @Async("virtualThreadPool")
  @GetMapping("/court/{courtId}")
  public CompletableFuture<Result<?>> getReservationsByCourtId(
          @PathVariable Integer courtId,
          @RequestHeader(value = "Authorization") String authorization) {
      try {
          Integer currentUserId = getUserIdFromToken(authorization);
          return CompletableFuture.completedFuture(Result.success(
                  reservationService.getReservationsByCourtId(courtId, currentUserId)));
      } catch (Exception e) {
          e.printStackTrace();
          return CompletableFuture.completedFuture(Result.fail(500, "获取场地预约失败：" + e.getMessage()));
      }
  }

    private Integer getUserIdFromToken(String authorization) {
        if (authorization == null || authorization.isEmpty()) {
            return null;
        }
        return jwtUtil.getUserIdFromToken(jwtUtil.extractToken(authorization));
    }
}