package com.school.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 图书馆学习时长记录实体类
 */
@Data
public class LibraryStudyRecord {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 进入时间
     */
    private LocalDateTime enterTime;

    /**
     * 离开时间（NULL代表未离开）
     */
    private LocalDateTime leaveTime;

    /**
     * 单次学习分钟数
     */
    private Integer durationMinutes;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}