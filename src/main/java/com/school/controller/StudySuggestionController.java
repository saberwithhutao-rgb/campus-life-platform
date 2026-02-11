package com.school.controller;

import com.school.entity.Result;
import com.school.service.StudySuggestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CompletableFuture;

/**
 * 学习建议控制器
 */
@RestController
@RequestMapping("/api/study")
public class StudySuggestionController {

    @Autowired
    private StudySuggestionService studySuggestionService;

    // 任务缓存，存储任务ID和对应的CompletableFuture
    private final Map<String, CompletableFuture<Result>> taskCache = new ConcurrentHashMap<>();

    /**
     * 获取学习建议
     * 
     * @param timeRange 时间范围：today/week/month
     * @param userId    用户ID
     * @return 统一格式的响应结果
     */
    @GetMapping("/suggestions")
    public Result getStudySuggestions(
            @RequestParam(value = "timeRange", defaultValue = "today") String timeRange,
            @RequestParam(value = "userId", defaultValue = "1") Integer userId) {
        System.out.println("开始获取学习建议，用户ID: " + userId + ", 时间范围: " + timeRange);

        // 直接调用Service，Service已处理所有异常
        Result result = studySuggestionService.getStudySuggestions(userId, timeRange);

        System.out.println("获取学习建议完成，结果: " + (result.isSuccess() ? "成功" : "失败"));
        return result;
    }

    /**
     * 提交异步学习建议任务
     * 
     * @param timeRange 时间范围：today/week/month
     * @param userId    用户ID
     * @return 包含任务ID的响应
     */
    @GetMapping("/suggestions/async")
    public Result submitAsyncSuggestionsTask(
            @RequestParam(value = "timeRange", defaultValue = "today") String timeRange,
            @RequestParam(value = "userId", defaultValue = "1") Integer userId) {
        System.out.println("提交异步学习建议任务，用户ID: " + userId + ", 时间范围: " + timeRange);

        // 生成唯一任务ID
        String taskId = UUID.randomUUID().toString();

        // 异步执行任务
        CompletableFuture<Result> future = studySuggestionService.getStudySuggestionsAsync(userId, timeRange);

        // 存储任务到缓存
        taskCache.put(taskId, future);

        // 任务完成后从缓存中移除
        future.thenRun(() -> {
            taskCache.remove(taskId);
            System.out.println("异步任务完成并从缓存中移除: " + taskId);
        });

        // 返回任务ID
        return Result.success()
                .data("taskId", taskId)
                .data("message", "任务已提交，正在处理中");
    }

    /**
     * 查询异步任务结果
     * 
     * @param taskId 任务ID
     * @return 任务执行结果
     */
    @GetMapping("/suggestions/query")
    public Result querySuggestionsTask(
            @RequestParam(value = "taskId") String taskId) {
        System.out.println("查询异步任务结果，任务ID: " + taskId);

        CompletableFuture<Result> future = taskCache.get(taskId);

        if (future == null) {
            // 任务不存在或已完成
            return Result.error("任务不存在或已完成")
                    .data("suggestions", new ArrayList<>());
        }

        if (future.isDone()) {
            // 任务已完成，返回结果
            try {
                Result taskResult = future.get();
                System.out.println("任务已完成，返回结果");
                return taskResult;
            } catch (Exception e) {
                System.err.println("获取任务结果失败: " + e.getMessage());
                e.printStackTrace();

                return Result.error("获取任务结果失败")
                        .data("suggestions", new ArrayList<>());
            }
        } else {
            // 任务正在处理中
            return Result.error("任务正在处理中，请稍后查询")
                    .data("suggestions", new ArrayList<>());
        }
    }
}
