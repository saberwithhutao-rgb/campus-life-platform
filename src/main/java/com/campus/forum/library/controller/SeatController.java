package com.campus.forum.library.controller;

import com.campus.forum.common.Result;
import com.campus.forum.common.util.RedisRateLimiterUtil;
import com.campus.forum.library.entity.Seat;
import com.campus.forum.library.service.SeatService;
import com.campus.forum.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/library/seats")
public class SeatController {

    private static final Logger log = LoggerFactory.getLogger(SeatController.class);

    private final SeatService seatService;
    private final RedisRateLimiterUtil redisRateLimiterUtil;
    private final JwtUtil jwtUtil;

    // 构造函数注入
    public SeatController(SeatService seatService,
                          RedisRateLimiterUtil redisRateLimiterUtil,
                          JwtUtil jwtUtil) {
        this.seatService = seatService;
        this.redisRateLimiterUtil = redisRateLimiterUtil;
        this.jwtUtil = jwtUtil;
    }

    @GetMapping("/classroom/{classroomId}")
    public Result<List<Seat>> getSeatsByClassroomId(
            @PathVariable Integer classroomId,
            @RequestHeader(value = "Authorization") String authorization) {

        Integer userId = extractUserId(authorization);
        if (userId == null) {
            return Result.fail(401, "未授权，请先登录");  // ← 直接返回，不继续执行
        }

        if (!redisRateLimiterUtil.checkQueryLimit(userId.toString())) {
            return Result.fail(429, "请求过于频繁，请稍后再试");
        }

        try {
            List<Seat> seats = seatService.getSeatsByClassroomId(classroomId);
            return Result.success(seats);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.fail(500, "获取座位列表失败：" + e.getMessage());
        }
    }

    // 参照 ReservationController 的写法
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