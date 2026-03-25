package com.campus.forum.common.util;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 * Redis 数据缓存工具类 - 高并发预约模块专用
 * 用于缓存楼层/场馆信息、教室/场地状态、座位/场地当前预约等信息
 */
@Component
public class RedisCacheUtil {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    // 缓存过期时间配置
    private static final int FLOOR_CACHE_EXPIRE = 3600; // 1小时
    private static final int VENUE_CACHE_EXPIRE = 3600; // 1小时
    private static final int CLASSROOM_CACHE_EXPIRE = 1800; // 30分钟
    private static final int COURT_CACHE_EXPIRE = 1800; // 30分钟
    private static final int SEAT_STATUS_CACHE_EXPIRE = 600; // 10分钟
    private static final int RESERVATION_CACHE_EXPIRE = 300; // 5分钟

    /**
     * 设置缓存
     * 
     * @param key    缓存键
     * @param value  缓存值
     * @param expire 过期时间（秒）
     */
    public void setCache(String key, Object value, int expire) {
        redisTemplate.opsForValue().set(key, value, expire, TimeUnit.SECONDS);
    }

    /**
     * 获取缓存
     * 
     * @param key 缓存键
     * @return 缓存值
     */
    @SuppressWarnings("unchecked")
    public <T> T getCache(String key) {
        T value = (T) redisTemplate.opsForValue().get(key);
        // 处理 GenericJackson2JsonRedisSerializer 序列化的 List 格式
        if (value != null) {
            // 检查是否是包含 "java.util.ArrayList" 的格式
            String valueStr = value.toString();
            if (valueStr.startsWith("[java.util.ArrayList, [")) {
                // 直接返回 null，让系统从数据库重新查询
                System.out.println("[缓存命中 - 旧格式数据] key: " + key + ", 清除旧格式缓存");
                redisTemplate.delete(key);
                return null;
            }
            System.out.println("[缓存命中] key: " + key + ", 数据: " + value);
        } else {
            System.out.println("[缓存未命中] key: " + key + ", 从数据库查询");
        }
        return value;
    }

    /**
     * 删除缓存
     * 
     * @param key 缓存键
     */
    public void deleteCache(String key) {
        redisTemplate.delete(key);
    }

    /**
     * 生成楼层缓存键
     * 
     * @param floorId 楼层ID
     * @return 缓存键
     */
    public String generateFloorCacheKey(Long floorId) {
        return "library:floor:" + floorId;
    }

    /**
     * 生成场馆缓存键
     * 
     * @param venueId 场馆ID
     * @return 缓存键
     */
    public String generateVenueCacheKey(Long venueId) {
        return "sports:venue:" + venueId;
    }

    /**
     * 生成教室缓存键
     * 
     * @param classroomId 教室ID
     * @return 缓存键
     */
    public String generateClassroomCacheKey(Long classroomId) {
        return "library:classroom:" + classroomId;
    }

    /**
     * 生成场地缓存键
     * 
     * @param courtId 场地ID
     * @return 缓存键
     */
    public String generateCourtCacheKey(Long courtId) {
        return "sports:court:" + courtId;
    }

    /**
     * 生成座位状态缓存键
     * 
     * @param seatId 座位ID
     * @param date   日期
     * @return 缓存键
     */
    public String generateSeatStatusCacheKey(Long seatId, String date) {
        return "library:seat:status:" + seatId + ":" + date;
    }

    /**
     * 生成场地状态缓存键
     * 
     * @param courtId 场地ID
     * @param date    日期
     * @return 缓存键
     */
    public String generateCourtStatusCacheKey(Long courtId, String date) {
        return "sports:court:status:" + courtId + ":" + date;
    }

    /**
     * 生成预约缓存键
     * 
     * @param reservationId 预约ID
     * @return 缓存键
     */
    public String generateReservationCacheKey(Long reservationId) {
        return "library:reservation:" + reservationId;
    }

    /**
     * 生成场馆预约缓存键
     * 
     * @param reservationId 预约ID
     * @return 缓存键
     */
    public String generateVenueReservationCacheKey(Long reservationId) {
        return "sports:reservation:" + reservationId;
    }

    // 以下是具体的缓存操作方法

    /**
     * 缓存楼层信息
     * 
     * @param floorId 楼层ID
     * @param floor   楼层信息
     */
    public void cacheFloor(Long floorId, Object floor) {
        String key = generateFloorCacheKey(floorId);
        setCache(key, floor, FLOOR_CACHE_EXPIRE);
    }

    /**
     * 获取缓存的楼层信息
     * 
     * @param floorId 楼层ID
     * @return 楼层信息
     */
    public <T> T getCachedFloor(Long floorId) {
        String key = generateFloorCacheKey(floorId);
        return getCache(key);
    }

    /**
     * 缓存场馆信息
     * 
     * @param venueId 场馆ID
     * @param venue   场馆信息
     */
    public void cacheVenue(Long venueId, Object venue) {
        String key = generateVenueCacheKey(venueId);
        setCache(key, venue, VENUE_CACHE_EXPIRE);
    }

    /**
     * 获取缓存的场馆信息
     * 
     * @param venueId 场馆ID
     * @return 场馆信息
     */
    public <T> T getCachedVenue(Long venueId) {
        String key = generateVenueCacheKey(venueId);
        return getCache(key);
    }

    /**
     * 缓存座位状态
     * 
     * @param seatId 座位ID
     * @param date   日期
     * @param status 状态
     */
    public void cacheSeatStatus(Long seatId, String date, Object status) {
        String key = generateSeatStatusCacheKey(seatId, date);
        setCache(key, status, SEAT_STATUS_CACHE_EXPIRE);
    }

    /**
     * 获取缓存的座位状态
     * 
     * @param seatId 座位ID
     * @param date   日期
     * @return 状态
     */
    public <T> T getCachedSeatStatus(Long seatId, String date) {
        String key = generateSeatStatusCacheKey(seatId, date);
        return getCache(key);
    }

    /**
     * 缓存场地状态
     * 
     * @param courtId 场地ID
     * @param date    日期
     * @param status  状态
     */
    public void cacheCourtStatus(Long courtId, String date, Object status) {
        String key = generateCourtStatusCacheKey(courtId, date);
        setCache(key, status, SEAT_STATUS_CACHE_EXPIRE);
    }

    /**
     * 获取缓存的场地状态
     * 
     * @param courtId 场地ID
     * @param date    日期
     * @return 状态
     */
    public <T> T getCachedCourtStatus(Long courtId, String date) {
        String key = generateCourtStatusCacheKey(courtId, date);
        return getCache(key);
    }

    /**
     * 清除预约相关缓存
     * 
     * @param seatId 座位ID
     * @param date   日期
     */
    public void clearReservationCache(Long seatId, String date) {
        String statusKey = generateSeatStatusCacheKey(seatId, date);
        deleteCache(statusKey);
    }

    /**
     * 清除场馆预约相关缓存
     * 
     * @param courtId 场地ID
     * @param date    日期
     */
    public void clearVenueReservationCache(Long courtId, String date) {
        String statusKey = generateCourtStatusCacheKey(courtId, date);
        deleteCache(statusKey);
    }
}
