package com.campus.forum.library.util;

import java.time.LocalTime;

public class TimeUtils {

    /**
     * 检查两个时间段是否重叠
     * 
     * @param start1 第一个时间段开始时间
     * @param end1   第一个时间段结束时间
     * @param start2 第二个时间段开始时间
     * @param end2   第二个时间段结束时间
     * @return 是否重叠
     */
    public static boolean isOverlapping(LocalTime start1, LocalTime end1, LocalTime start2, LocalTime end2) {
        return (start1.isBefore(end2) && end1.isAfter(start2));
    }

    /**
     * 计算结束时间
     * 
     * @param startTime 开始时间
     * @param duration  时长（分钟）
     * @return 结束时间
     */
    public static LocalTime calculateEndTime(LocalTime startTime, int duration) {
        return startTime.plusMinutes(duration);
    }

    /**
     * 检查当前时间是否在预约开始时间后30分钟或更晚
     * 
     * @param reserveDate 预约日期
     * @param startTime   预约开始时间
     * @return 是否满足条件
     */
    public static boolean isAfter30MinutesFromStart(java.time.LocalDate reserveDate, java.time.LocalTime startTime) {
        java.time.LocalDateTime startDateTime = java.time.LocalDateTime.of(reserveDate, startTime);
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        return now.isAfter(startDateTime.plusMinutes(30));
    }

    /**
     * 检查预约时间是否合法
     * 
     * @param reserveDate 预约日期
     * @param startTime   预约开始时间
     * @return 是否合法
     */
    public static boolean isReservationTimeValid(java.time.LocalDate reserveDate, java.time.LocalTime startTime) {
        java.time.LocalDateTime reservationTime = java.time.LocalDateTime.of(reserveDate, startTime);
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        return reservationTime.isAfter(now);
    }
}