package com.school.controller;

import com.school.service.StudyStatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 学习统计控制器
 */
@RestController
@RequestMapping("/api/study")
public class StudyStatisticsController {

    @Autowired
    private StudyStatisticsService studyStatisticsService;

    /**
     * 获取学习统计数据
     * 
     * @param timeRange 时间范围：today/week/month
     * @param userId    用户ID
     * @return 统计数据Map
     */
    @GetMapping("/statistics")
    public Map<String, Object> getStudyStatistics(
            @RequestParam("timeRange") String timeRange,
            @RequestParam("userId") Integer userId) {
        return studyStatisticsService.getStudyStatistics(userId, timeRange);
    }
}