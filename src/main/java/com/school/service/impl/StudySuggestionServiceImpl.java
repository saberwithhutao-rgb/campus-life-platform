package com.school.service.impl;

import com.school.service.QianwenService;
import com.school.service.StudySuggestionService;
import com.school.service.StudyStatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.school.entity.Result;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 学习建议服务实现类
 */
@Service
public class StudySuggestionServiceImpl implements StudySuggestionService {

    @Autowired
    private StudyStatisticsService studyStatisticsService;

    @Autowired
    private QianwenService qianwenService;

    // 异步执行线程池
    private final ExecutorService executorService = Executors.newFixedThreadPool(5);

    /**
     * 获取用户学习建议
     *
     * @param userId    用户ID
     * @param timeRange 时间范围：today/week/month
     * @return 统一格式的响应结果
     */
    @Override
    public Result getStudySuggestions(Integer userId, String timeRange) {
        System.out.println("开始获取用户学习建议，用户ID: " + userId + ", 时间范围: " + timeRange);

        try {
            // 获取用户学习统计数据
            Map<String, Object> statistics = studyStatisticsService.getStudyStatistics(userId, timeRange);

            System.out.println("获取到学习统计数据，包含: " + statistics.keySet().size() + " 个字段");

            // 构建统计数据描述
            String statisticsText = buildStatisticsDescription(statistics);

            System.out.println("调用千问API生成学习建议");
            // 调用千问API生成学习建议
            String suggestionsText = qianwenService.generateStudySuggestions(statisticsText, timeRange);

            System.out.println("千问API返回的建议文本: " + suggestionsText);

            // 检查是否为默认文本
            if (suggestionsText.contains("学习计划完成率") || suggestionsText.contains("建议适当增加")) {
                System.out.println("检测到默认文本，重新调用AI");
                // 如果是默认文本，重新调用AI
                suggestionsText = qianwenService.generateStudySuggestions(statisticsText, timeRange);
            }

            // 解析返回的建议文本
            List<String> parsedSuggestions = parseSuggestions(suggestionsText);
            System.out.println("成功解析 " + parsedSuggestions.size() + " 条建议");

            // 构建成功响应，确保包含suggestions数组
            return Result.success()
                    .data("suggestions", parsedSuggestions)
                    .data("message", "获取学习建议成功");
        } catch (Exception e) {
            System.err.println("获取学习建议失败: " + e.getMessage());
            e.printStackTrace();

            // 构建错误响应，确保包含suggestions空数组
            return Result.error("获取学习建议失败，请稍后重试")
                    .data("suggestions", new ArrayList<>());
        }
    }

    /**
     * 获取默认学习建议（当AI调用失败时使用）
     */
    private List<String> getDefaultSuggestions() {
        List<String> suggestions = new ArrayList<>();
        suggestions.add("根据您的学习数据，您的学习计划完成率达到了60%，表现不错！建议您继续保持当前的学习节奏，同时可以适当增加一些挑战性任务来提升自己的能力。");
        suggestions.add("建议平衡各类计划的分配，保持全面发展。根据您的学习统计数据，建议您在保持现有学习进度的同时，适当增加一些高难度任务来挑战自己，提升学习能力。");
        suggestions.add("建议适当增加一些挑战性的任务，挑战自己。根据您的学习情况，建议您在保持稳定学习的同时，适当增加一些创新性的学习内容，拓展知识面。");
        suggestions.add("建议保持规律的学习习惯，定期回顾和总结学习成果。根据您的学习数据，建议您建立更系统的学习计划，定期检查学习进度，及时调整学习策略。");
        return suggestions;
    }

    /**
     * 构建统计数据描述文本
     */
    private String buildStatisticsDescription(Map<String, Object> statistics) {
        StringBuilder sb = new StringBuilder();

        int totalPlanCount = (int) statistics.get("totalPlanCount");
        int completedPlanCount = (int) statistics.get("completedPlanCount");
        double completionRate = (double) statistics.get("completionRate");
        double averageProgress = (double) statistics.get("averageProgress");
        int overduePlanCount = (int) statistics.get("overduePlanCount");
        Map<String, Object> difficultyDistribution = (Map<String, Object>) statistics.get("difficultyDistribution");
        Map<String, Object> planTypeDistribution = (Map<String, Object>) statistics.get("planTypeDistribution");
        Map<String, Object> subjectDistribution = (Map<String, Object>) statistics.get("subjectDistribution");

        sb.append("总计划数: ").append(totalPlanCount).append("\n");
        sb.append("已完成计划数: ").append(completedPlanCount).append("\n");
        sb.append("完成率: ").append(String.format("%.2f%%", completionRate * 100)).append("\n");
        sb.append("平均进度: ").append(String.format("%.2f%%", averageProgress)).append("\n");
        sb.append("延期计划数: ").append(overduePlanCount).append("\n");

        sb.append("\n难度分布:\n");
        for (Map.Entry<String, Object> entry : difficultyDistribution.entrySet()) {
            sb.append("  - ").append(entry.getKey()).append(": ").append(entry.getValue()).append("个\n");
        }

        sb.append("\n计划类型分布:\n");
        for (Map.Entry<String, Object> entry : planTypeDistribution.entrySet()) {
            sb.append("  - ").append(entry.getKey()).append(": ").append(entry.getValue()).append("个\n");
        }

        sb.append("\n科目分布:\n");
        for (Map.Entry<String, Object> entry : subjectDistribution.entrySet()) {
            sb.append("  - ").append(entry.getKey()).append(": ").append(entry.getValue()).append("个\n");
        }

        return sb.toString();
    }

    /**
     * 解析千问返回的建议文本
     */
    private List<String> parseSuggestions(String suggestionsText) {
        List<String> suggestions = new ArrayList<>();

        if (suggestionsText == null || suggestionsText.trim().isEmpty()) {
            return suggestions;
        }

        // 按换行符分割建议
        String[] lines = suggestionsText.split("\n");
        for (String line : lines) {
            String trimmedLine = line.trim();
            if (!trimmedLine.isEmpty()) {
                suggestions.add(trimmedLine);
            }
        }

        // 限制返回3-4条建议
        if (suggestions.size() > 4) {
            return suggestions.subList(0, 4);
        }

        return suggestions;
    }

    /**
     * 异步获取用户学习建议
     */
    @Override
    public CompletableFuture<Result> getStudySuggestionsAsync(Integer userId, String timeRange) {
        return CompletableFuture.supplyAsync(() -> {
            return getStudySuggestions(userId, timeRange);
        }, executorService);
    }
}
