package com.school.service;

import java.util.concurrent.CompletableFuture;

/**
 * 千问AI服务接口
 */
public interface QianwenService {
    
    /**
     * 调用千问API生成学习建议
     * 
     * @param statistics 学习统计数据
     * @param timeRange 时间范围
     * @return 生成的学习建议
     */
    String generateStudySuggestions(String statistics, String timeRange);
    
    /**
     * 调用千问API进行文本生成
     * 
     * @param prompt 提示词
     * @return 生成的文本
     */
    String generateText(String prompt);
    
    /**
     * 异步调用千问API生成学习建议
     * 
     * @param statistics 学习统计数据
     * @param timeRange 时间范围
     * @return 包含生成学习建议的CompletableFuture
     */
    CompletableFuture<String> generateStudySuggestionsAsync(String statistics, String timeRange);
    
    /**
     * 异步调用千问API进行文本生成
     * 
     * @param prompt 提示词
     * @return 包含生成文本的CompletableFuture
     */
    CompletableFuture<String> generateTextAsync(String prompt);
}
