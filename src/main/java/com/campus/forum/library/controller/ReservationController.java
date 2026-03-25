package com.campus.forum.library.controller;

import com.campus.forum.common.Result;
import com.campus.forum.library.entity.Reservation;
import com.campus.forum.library.dto.ReservationDTO;
import com.campus.forum.library.service.ReservationService;
import com.campus.forum.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/library/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;
    private final JwtUtil jwtUtil;

    @PostMapping
    public Result<Reservation> createReservation(
            @RequestBody ReservationDTO reservationDTO,
            @RequestHeader(value = "Authorization") String authorization) {

        Integer userId = extractUserId(authorization);
        if (userId == null) {
            return Result.fail(401, "未授权或Token无效");
        }

        try {
            Reservation reservation = reservationService.createReservation(reservationDTO, userId);
            log.info("创建预约成功, userId: {}, seatId: {}", userId, reservationDTO.getSeatId());
            return Result.success(reservation);
        } catch (IllegalArgumentException e) {
            log.warn("创建预约参数错误: {}", e.getMessage());
            return Result.fail(400, e.getMessage());
        } catch (RuntimeException e) {
            // 业务异常：如"不能预约已过去的时间段"、"该时间段已被预约"等
            log.warn("创建预约业务异常: {}", e.getMessage());
            return Result.fail(400, e.getMessage());
        } catch (Exception e) {
            log.error("创建预约系统异常, userId: {}, seatId: {}", userId, reservationDTO.getSeatId(), e);
            return Result.fail(500, "服务器内部错误，请稍后重试");
        }
    }

    @PostMapping("/{id}/occupy")
    public Result<Reservation> occupySeat(
            @PathVariable Integer id,
            @RequestHeader(value = "Authorization") String authorization) {

        Integer userId = extractUserId(authorization);
        if (userId == null) {
            return Result.fail(401, "未授权或Token无效");
        }

        try {
            Reservation reservation = reservationService.occupySeat(id, userId);
            log.info("占用座位成功, reservationId: {}, userId: {}", id, userId);
            return Result.success(reservation);
        } catch (IllegalArgumentException e) {
            log.warn("占用座位参数错误: {}", e.getMessage());
            return Result.fail(400, e.getMessage());
        } catch (RuntimeException e) {
            log.warn("占用座位业务异常: {}", e.getMessage());
            return Result.fail(400, e.getMessage());
        } catch (Exception e) {
            log.error("占用座位系统异常, reservationId: {}, userId: {}", id, userId, e);
            return Result.fail(500, "服务器内部错误，请稍后重试");
        }
    }

    @PostMapping("/{id}/leave")
    public Result<?> leaveSeat(
            @PathVariable Integer id,
            @RequestHeader(value = "Authorization") String authorization) {

        Integer userId = extractUserId(authorization);
        if (userId == null) {
            return Result.fail(401, "未授权或Token无效");
        }

        try {
            reservationService.leaveSeat(id, userId);
            log.info("离开座位成功, reservationId: {}, userId: {}", id, userId);
            return Result.success(null);
        } catch (IllegalArgumentException e) {
            log.warn("离开座位参数错误: {}", e.getMessage());
            return Result.fail(400, e.getMessage());
        } catch (RuntimeException e) {
            log.warn("离开座位业务异常: {}", e.getMessage());
            return Result.fail(400, e.getMessage());
        } catch (Exception e) {
            log.error("离开座位系统异常, reservationId: {}, userId: {}", id, userId, e);
            return Result.fail(500, "服务器内部错误，请稍后重试");
        }
    }

    @GetMapping("/user")
    public Result<List<Reservation>> getCurrentUserReservations(
            @RequestHeader(value = "Authorization") String authorization) {

        Integer userId = extractUserId(authorization);
        if (userId == null) {
            return Result.fail(401, "未授权或Token无效");
        }

        try {
            List<Reservation> reservations = reservationService.getReservationsByUserId(userId);
            log.info("获取用户预约记录成功, userId: {}, 数量: {}", userId, reservations.size());
            return Result.success(reservations);
        } catch (Exception e) {
            log.error("获取用户预约记录失败, userId: {}", userId, e);
            return Result.fail(500, "获取预约记录失败");
        }
    }

    @GetMapping("/seat/{seatId}")
    public Result<List<Reservation>> getReservationBySeatId(@PathVariable Integer seatId) {
        try {
            List<Reservation> reservations = reservationService.getReservationBySeatId(seatId);
            return Result.success(reservations);
        } catch (Exception e) {
            log.error("获取座位预约失败, seatId: {}", seatId, e);
            return Result.fail(500, "获取座位预约失败");
        }
    }

    private Integer extractUserId(String authorization) {
        if (authorization == null || authorization.isEmpty()) {
            log.warn("Authorization header为空");
            return null;
        }

        try {
            String token = jwtUtil.extractToken(authorization);
            if (token == null || token.isEmpty()) {
                log.warn("Token为空");
                return null;
            }
            return jwtUtil.getUserIdFromToken(token);
        } catch (Exception e) {
            log.warn("Token解析失败: {}", e.getMessage());
            return null;
        }
    }
}