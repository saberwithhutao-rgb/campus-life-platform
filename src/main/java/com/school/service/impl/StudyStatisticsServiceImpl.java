package com.school.service.impl;

import com.school.mapper.StudyPlanMapper;
import com.school.service.StudyStatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 学习统计服务实现类
 */
@Service
public class StudyStatisticsServiceImpl implements StudyStatisticsService {

    @Autowired
    private StudyPlanMapper studyPlanMapper;

    // 日期格式化器
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    // 小数格式化器，保留两位小数
    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#.00");

    /**
     * 获取用户学习统计数据
     * 
     * @param userId    用户ID
     * @param timeRange 时间范围：today/week/month
     * @return 统计数据Map
     */
    @Override
    public Map<String, Object> getStudyStatistics(Integer userId, String timeRange) {
        System.out.println("开始获取学习统计数据，用户ID: " + userId + ", 时间范围: " + timeRange);
        
        // 计算时间范围
        Map<String, String> timeRangeMap = calculateTimeRange(timeRange);
        String startTime = timeRangeMap.get("startTime");
        String endTime = timeRangeMap.get("endTime");
        String currentDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        System.out.println("计算的时间范围: " + startTime + " 到 " + endTime);

        // 1. 获取总计划数
        System.out.println("开始查询总计划数");
        int totalPlanCount = studyPlanMapper.getTotalPlanCount(userId, startTime, endTime);
        System.out.println("获取到总计划数: " + totalPlanCount);

        // 2. 获取已完成计划数
        System.out.println("开始查询已完成计划数");
        int completedPlanCount = studyPlanMapper.getCompletedPlanCount(userId, startTime, endTime);
        System.out.println("获取到已完成计划数: " + completedPlanCount);

        // 3. 计算计划完成率
        double completionRate = 0.0;
        if (totalPlanCount > 0) {
            completionRate = (double) completedPlanCount / totalPlanCount;
        }
        System.out.println("计算完成率: " + String.format("%.2f%%", completionRate * 100));

        // 4. 获取平均进度
        System.out.println("开始查询平均进度");
        Double averageProgressDouble = studyPlanMapper.getAverageProgress(userId, startTime, endTime);
        double averageProgress = averageProgressDouble != null ? averageProgressDouble : 0.0;
        System.out.println("获取到平均进度: " + String.format("%.2f%%", averageProgress));

        // 5. 获取延期计划数
        System.out.println("开始查询延期计划数");
        int overduePlanCount = studyPlanMapper.getOverduePlanCount(userId, currentDate, startTime, endTime);
        System.out.println("获取到延期计划数: " + overduePlanCount);

        // 6. 获取难度分布
        System.out.println("开始查询难度分布");
        List<Map<String, Object>> difficultyDistributionList = studyPlanMapper.getDifficultyDistribution(userId,
                startTime, endTime);
        Map<String, Object> difficultyDistribution = formatDistribution(difficultyDistributionList, totalPlanCount);
        System.out.println("获取到难度分布: " + difficultyDistribution.size() + " 个难度级别");

        // 7. 获取计划类型分布
        System.out.println("开始查询计划类型分布");
        List<Map<String, Object>> planTypeDistributionList = studyPlanMapper.getPlanTypeDistribution(userId, startTime,
                endTime);
        Map<String, Object> planTypeDistribution = formatDistribution(planTypeDistributionList, totalPlanCount);
        System.out.println("获取到计划类型分布: " + planTypeDistribution.size() + " 个类型");

        // 8. 获取各科目计划数量
        System.out.println("开始查询科目分布");
        List<Map<String, Object>> subjectDistributionList = studyPlanMapper.getSubjectDistribution(userId, startTime,
                endTime);
        Map<String, Object> subjectDistribution = new HashMap<>();
        for (Map<String, Object> item : subjectDistributionList) {
            String subject = (String) item.get("subject");
            // 处理PostgreSQL返回的Long类型
            Number countNum = (Number) item.get("count");
            Integer count = countNum.intValue();
            subjectDistribution.put(subject, count);
        }
        System.out.println("获取到科目分布: " + subjectDistribution.size() + " 个科目");

        // 计算未完成计划数
        int unfinishedCount = totalPlanCount - completedPlanCount;
        System.out.println("计算未完成计划数: " + unfinishedCount);

        // 构建返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("totalPlanCount", totalPlanCount);
        result.put("completedPlanCount", completedPlanCount);
        result.put("unfinishedCount", unfinishedCount);
        result.put("completionRate", Double.parseDouble(DECIMAL_FORMAT.format(completionRate)));
        result.put("difficultyDistribution", difficultyDistribution);
        result.put("planTypeDistribution", planTypeDistribution);
        result.put("averageProgress", Double.parseDouble(DECIMAL_FORMAT.format(averageProgress)));
        result.put("overduePlanCount", overduePlanCount);
        result.put("subjectDistribution", subjectDistribution);

        System.out.println("成功获取学习统计数据，返回 " + result.size() + " 个字段");
        return result;
    }

    /**
     * 计算时间范围
     * 
     * @param timeRange 时间范围类型：today/week/month
     * @return 包含开始时间和结束时间的Map
     */
    private Map<String, String> calculateTimeRange(String timeRange) {
        Map<String, String> result = new HashMap<>();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startTime = switch (timeRange) {
            case "today" ->
                // 今天：从今天00:00:00到现在
                    now.withHour(0).withMinute(0).withSecond(0);
            case "week" ->
                // 过去一周：从7天前到现在
                    now.minusDays(7);
            case "month" ->
                // 过去一个月：从30天前到现在
                    now.minusDays(30);
            default ->
                // 默认：今天
                    now.withHour(0).withMinute(0).withSecond(0);
        };

        result.put("startTime", startTime.format(FORMATTER));
        result.put("endTime", now.format(FORMATTER));
        return result;
    }

    /**
     * 格式化分布数据，计算占比
     * 
     * @param distributionList 分布数据列表
     * @param totalCount       总数
     * @return 格式化后的分布数据Map
     */
    private Map<String, Object> formatDistribution(List<Map<String, Object>> distributionList, int totalCount) {
        System.out.println("=== formatDistribution 详细日志开始 ===");
        System.out.println("distributionList 原始数据: " + distributionList);
        System.out.println("distributionList 大小: " + distributionList.size());
        System.out.println("totalCount: " + totalCount);

        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> details = new ArrayList<>();

        for (Map<String, Object> item : distributionList) {
            System.out.println("处理单个item: " + item);
            System.out.println("item的keys: " + item.keySet());

            String key = (String) item.get("difficulty") != null ? (String) item.get("difficulty")
                    : (String) item.get("plan_type");
            System.out.println("提取的key: " + key);

            Number countNum = (Number) item.get("count");
            System.out.println("countNum: " + countNum);
            Integer count = countNum.intValue();
            System.out.println("转换后的count: " + count);

            double percentage = 0.0;
            if (totalCount > 0) {
                percentage = (double) count / totalCount;
            }
            System.out.println("计算的percentage: " + percentage);

            Map<String, Object> detail = new HashMap<>();
            detail.put("type", key);
            detail.put("count", count);
            detail.put("percentage", Double.parseDouble(DECIMAL_FORMAT.format(percentage)));
            details.add(detail);
            System.out.println("添加的detail: " + detail);

            result.put(key, count);
        }

        result.put("details", details);
        System.out.println("最终的details: " + details);
        System.out.println("最终的result: " + result);
        System.out.println("=== formatDistribution 详细日志结束 ===");
        return result;
    }
}