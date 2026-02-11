package com.school.service.impl;

import com.school.entity.LibraryStudyRecord;
import com.school.mapper.LibraryStudyRecordMapper;
import com.school.service.LibraryStudyRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 图书馆学习时长记录服务实现类
 */
@Service
public class LibraryStudyRecordServiceImpl implements LibraryStudyRecordService {

    @Autowired
    private LibraryStudyRecordMapper libraryStudyRecordMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * 日期格式化器（用于获取今日凌晨0点）
     */
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd 00:00:00");

    /**
     * 进入图书馆
     * 
     * @param userId 用户ID
     * @return 插入记录的ID
     */
    @Override
    public Long enterLibrary(Long userId) {
        // 创建新记录
        LibraryStudyRecord record = new LibraryStudyRecord();
        record.setUserId(userId);
        record.setEnterTime(LocalDateTime.now()); // 自动取当前时间
        record.setLeaveTime(null); // 未离开
        record.setDurationMinutes(0); // 默认0
        record.setCreateTime(LocalDateTime.now());
        record.setUpdateTime(LocalDateTime.now());

        // 插入记录（MyBatis会自动设置生成的ID到实体类中）
        libraryStudyRecordMapper.insert(record);

        // 返回插入记录的ID（从实体类中获取）
        return record.getId();
    }

    /**
     * 离开图书馆
     * 
     * @param recordId 记录ID
     * @return 本次学习时长（分钟），失败返回0
     */
    @Override
    public Integer leaveLibrary(Long recordId) {
        System.out.println("=== 离开图书馆接口调用 ===");
        System.out.println("传入的 recordId: " + recordId);

        // 1. 首先查询记录是否存在
        System.out.println("开始查询记录...");
        LibraryStudyRecord record = libraryStudyRecordMapper.selectById(recordId);
        System.out.println("查询结果: " + (record != null ? "记录存在" : "记录不存在"));

        // 2. 检查记录是否存在
        if (record == null) {
            System.out.println("记录不存在，返回-1");
            return -1; // 记录不存在
        }

        // 3. 检查记录是否已离开
        System.out.println("记录的 leaveTime: " + record.getLeaveTime());
        if (record.getLeaveTime() != null) {
            System.out.println("记录已离开，返回-1");
            return -1; // 记录已离开
        }

        // 4. 更新离开时间为当前时间
        LocalDateTime leaveTime = LocalDateTime.now();
        record.setLeaveTime(leaveTime);
        System.out.println("设置离开时间: " + leaveTime);

        // 5. 计算学习时长（分钟）
        long durationMinutes = java.time.Duration.between(record.getEnterTime(), leaveTime).toMinutes();
        record.setDurationMinutes((int) durationMinutes);
        System.out.println("计算时长: " + durationMinutes + "分钟");

        // 6. 更新时间
        record.setUpdateTime(LocalDateTime.now());

        // 7. 保存更新
        System.out.println("开始更新记录...");
        libraryStudyRecordMapper.updateById(record);
        System.out.println("更新完成");

        // 8. 返回计算好的时长
        System.out.println("返回时长: " + record.getDurationMinutes());
        System.out.println("=== 离开图书馆接口调用结束 ===");
        return record.getDurationMinutes();
    }

    /**
     * 查询今日总学习时长
     * 
     * @param userId 用户ID
     * @return 今日总学习时长（分钟）
     */
    @Override
    public Integer getTodayTotalDuration(Long userId) {
        // 获取今日凌晨0点
        String todayStart = LocalDateTime.now().format(DATE_FORMATTER);

        // 调用Mapper方法查询今日总时长
        return libraryStudyRecordMapper.getTodayTotalDuration(userId, todayStart);
    }
}