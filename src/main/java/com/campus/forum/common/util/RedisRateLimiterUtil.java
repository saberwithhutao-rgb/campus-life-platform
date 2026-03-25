package com.campus.forum.common.util;

import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 * Redis 限流工具类 - 高并发预约模块专用
 * 用于防止用户恶意刷接口，实现基于 Redis + Lua 脚本的限流
 */
@Component
public class RedisRateLimiterUtil {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 限流检查
     * @param userId 用户ID
     * @param keyPrefix 键前缀
     * @param limit 时间窗口内的最大请求数
     * @param windowSize 时间窗口大小（秒）
     * @return 是否允许请求
     */
    public boolean isAllowed(String userId, String keyPrefix, int limit, int windowSize) {
        String key = keyPrefix + ":" + userId;
        
        // 使用 Lua 脚本实现滑动窗口限流
        String script = """
            local key = KEYS[1]
            local limit = tonumber(ARGV[1])
            local windowSize = tonumber(ARGV[2])
            local currentTime = tonumber(ARGV[3])
            
            -- 移除时间窗口外的记录
            redis.call('ZREMRANGEBYSCORE', key, 0, currentTime - windowSize)
            
            -- 获取当前窗口内的请求数
            local count = redis.call('ZCARD', key)
            
            -- 检查是否超过限制
            if count < limit then
                -- 添加当前请求时间戳
                redis.call('ZADD', key, currentTime, currentTime)
                -- 设置过期时间，避免内存泄漏
                redis.call('EXPIRE', key, windowSize)
                return count + 1
            else
                return -1
            end
        """;
        
        long currentTime = System.currentTimeMillis() / 1000;
        Object result = redisTemplate.execute((org.springframework.data.redis.core.RedisCallback<Object>) redisConnection -> {
            return redisConnection.eval(
                script.getBytes(),
                org.springframework.data.redis.connection.ReturnType.INTEGER,
                1,
                key.getBytes(),
                String.valueOf(limit).getBytes(),
                String.valueOf(windowSize).getBytes(),
                String.valueOf(currentTime).getBytes()
            );
        });
        
        if (result != null) {
            long count = (Long) result;
            if (count > 0) {
                System.out.println("[限流通过] userId: " + userId + ", 当前请求数: " + count);
                return true;
            } else {
                System.out.println("[限流被拒] userId: " + userId + ", 超过阈值: " + limit);
                return false;
            }
        }
        return false;
    }

    /**
     * 检查用户预约接口限流
     * @param userId 用户ID
     * @return 是否允许请求
     */
    public boolean checkReservationRateLimit(String userId) {
        // 单个用户每分钟最多调用 10 次预约/占用接口
        return isAllowed(userId, "reservation:rate:limit", 10, 60);
    }
}
