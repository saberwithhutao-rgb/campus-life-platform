package com.campus.forum.common.util;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 * Redis 分布式锁工具类 - 高并发预约模块专用
 * 用于解决高并发抢座/场地时的重复预约、超卖问题
 */
@Component
public class RedisDistributedLockUtil {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 获取分布式锁
     * @param lockKey 锁键
     * @param requestId 请求ID，用于标识锁的持有者
     * @param expireTime 过期时间（秒）
     * @return 是否获取成功
     */
    public boolean acquireLock(String lockKey, String requestId, int expireTime) {
        // 使用 SET NX EX 命令实现分布式锁
        // NX: 只有在键不存在时才设置
        // EX: 设置过期时间
        Boolean result = redisTemplate.opsForValue().setIfAbsent(lockKey, requestId, expireTime, TimeUnit.SECONDS);
        if (Boolean.TRUE.equals(result)) {
            System.out.println("[获取锁成功] lockKey: " + lockKey);
        } else {
            System.out.println("[获取锁失败] lockKey: " + lockKey);
        }
        return Boolean.TRUE.equals(result);
    }

    /**
     * 释放分布式锁
     *
     * @param lockKey   锁键
     * @param requestId 请求ID，用于验证锁的持有者
     */
    public void releaseLock(String lockKey, String requestId) {
        // 使用 Lua 脚本确保原子操作
        String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
        
        // 使用 RedisTemplate 的 execute 方法执行 Lua 脚本
        Object result = redisTemplate.execute((org.springframework.data.redis.core.RedisCallback<Object>) redisConnection -> {
            return redisConnection.eval(
                script.getBytes(),
                org.springframework.data.redis.connection.ReturnType.INTEGER,
                1,
                lockKey.getBytes(),
                requestId.getBytes()
            );
        });
        
        boolean success = Long.valueOf(1).equals(result);
        if (success) {
            System.out.println("[释放锁成功] lockKey: " + lockKey);
        }
    }

    /**
     * 生成锁键
     * @param prefix 前缀
     * @param id 资源ID
     * @param timeSlot 时间段
     * @return 锁键
     */
    public String generateLockKey(String prefix, String id, String timeSlot) {
        return prefix + ":" + id + ":" + timeSlot;
    }
}
