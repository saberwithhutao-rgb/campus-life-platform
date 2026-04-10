package com.campus.forum.common.util;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import jakarta.annotation.Resource;
import java.util.Map;

@Component
public class RedisRateLimiterUtil {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 限流检查 - 支持秒级+分钟级双层限流
     * @param userId 用户ID
     * @param actionType 操作类型
     * @param minuteLimit 分钟级限流次数
     * @param minuteWindow 分钟级窗口（秒）
     * @param secondLimit 秒级限流次数
     * @param secondWindow 秒级窗口（秒）
     */
    public boolean checkRateLimit(String userId, String actionType,
                                  int minuteLimit, int minuteWindow,
                                  int secondLimit, int secondWindow) {
        String minuteKey = "rate:minute:" + actionType + ":" + userId;
        String secondKey = "rate:second:" + actionType + ":" + userId;

        // 先检查秒级限流（更严格）
        if (!isAllowed(secondKey, secondLimit, secondWindow)) {
            System.out.println("[限流被拒-秒级] userId: " + userId + ", 1秒内超过" + secondLimit + "次");
            return false;
        }

        // 再检查分钟级限流
        if (!isAllowed(minuteKey, minuteLimit, minuteWindow)) {
            System.out.println("[限流被拒-分钟级] userId: " + userId + ", " + minuteWindow + "秒内超过" + minuteLimit + "次");
            return false;
        }

        return true;
    }

    // 便捷方法
    public boolean checkCreateLimit(String userId) {
        return checkRateLimit(userId, "create", 5, 60, 2, 1);  // 5次/分钟 + 1秒内最多2次
    }

    public boolean checkOccupyLimit(String userId) {
        return checkRateLimit(userId, "occupy", 3, 60, 1, 1);  // 3次/分钟 + 1秒内最多1次
    }

    public boolean checkQueryLimit(String userId) {
        return checkRateLimit(userId, "query", 30, 60, 5, 1);  // 30次/分钟 + 1秒内最多5次
    }

    /**
     * 核心限流逻辑（使用 Lua 脚本）
     * @param key Redis key
     * @param limit 时间窗口内的最大请求数
     * @param windowSize 时间窗口大小（秒）
     * @return 是否允许请求
     */
    private boolean isAllowed(String key, int limit, int windowSize) {
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
                redis.call('EXPIRE', key, windowSize)
                return 1
            else
                return 0
            end
        """;

        long currentTime = System.currentTimeMillis() / 1000;

        Object result = redisTemplate.execute((org.springframework.data.redis.core.RedisCallback<Object>) redisConnection -> redisConnection.eval(
                script.getBytes(),
                org.springframework.data.redis.connection.ReturnType.INTEGER,
                1,
                key.getBytes(),
                String.valueOf(limit).getBytes(),
                String.valueOf(windowSize).getBytes(),
                String.valueOf(currentTime).getBytes()
        ));

        boolean allowed = result != null && (Long) result == 1;

        // 日志记录（便于调试）
        if (!allowed) {
            System.out.println("[限流被拒] userId: " + key + ", 超过阈值: " + limit + "次/" + windowSize + "秒");
        }

        return allowed;
    }
}