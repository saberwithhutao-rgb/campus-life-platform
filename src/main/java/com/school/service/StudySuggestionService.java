package com.school.service;

import com.school.entity.Result;
import java.util.concurrent.CompletableFuture;

/**
 * 学习建议服务接口
 */
public interface StudySuggestionService {

    /**
     * 获取用户学习建议
     * 
     * @param userId    用户ID
     * @param timeRange 时间范围：today/week/month
     * @return 统一格式的响应结果
     */
    Result getStudySuggestions(Integer userId, String timeRange);

    /**
     * 异步获取用户学习建议
     * 
     * @param userId    用户ID
     * @param timeRange 时间范围：today/week/month
     * @return 包含统一格式响应结果的CompletableFuture
     */
    CompletableFuture<Result> getStudySuggestionsAsync(Integer userId, String timeRange);
}