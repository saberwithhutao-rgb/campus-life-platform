package com.school.service;

/**
 * 图书馆学习时长记录服务接口
 */
public interface LibraryStudyRecordService {

    /**
     * 进入图书馆
     * 
     * @param userId 用户ID
     * @return 插入记录的ID
     */
    Long enterLibrary(Long userId);

    /**
     * 离开图书馆
     * 
     * @param recordId 记录ID
     * @return 本次学习时长（分钟），失败返回0
     */
    Integer leaveLibrary(Long recordId);

    /**
     * 查询今日总学习时长
     * 
     * @param userId 用户ID
     * @return 今日总学习时长（分钟）
     */
    Integer getTodayTotalDuration(Long userId);
}