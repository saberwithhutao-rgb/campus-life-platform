package com.school.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.school.config.QianwenConfig;
import com.school.service.QianwenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 千问AI服务实现类
 */
@Service
public class QianwenServiceImpl implements QianwenService {

    @Autowired
    private QianwenConfig qianwenConfig;

    @Autowired
    private RestTemplate restTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();

    // 异步执行线程池
    private final ExecutorService executorService = Executors.newFixedThreadPool(5);

    /**
     * 调用千问API生成学习建议
     */
    @Override
    public String generateStudySuggestions(String statistics, String timeRange) {
        String prompt = buildStudySuggestionPrompt(statistics, timeRange);
        return generateText(prompt);
    }

    /**
     * 调用千问API进行文本生成
     */
    @Override
    public String generateText(String prompt) {
        try {
            // 构建请求体
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", qianwenConfig.getModel());

            Map<String, Object> input = new HashMap<>();
            input.put("messages", List.of(
                    Map.of("role", "user", "content", prompt)));
            requestBody.put("input", input);

            Map<String, Object> parameters = new HashMap<>();
            parameters.put("result_format", "message");
            requestBody.put("parameters", parameters);

            // 添加中文编码设置
            parameters.put("top_p", 0.8);
            parameters.put("top_k", 5);
            parameters.put("temperature", 0.7);
            parameters.put("max_tokens", 512);

            // 设置请求头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + qianwenConfig.getApiKey());
            headers.set("X-DashScope-SSE", "disable");

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            // 发送请求
            ResponseEntity<String> response = restTemplate.exchange(
                    qianwenConfig.getEndpoint(),
                    HttpMethod.POST,
                    entity,
                    String.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                // 解析响应
                JsonNode root = objectMapper.readTree(response.getBody());
                JsonNode output = root.path("output");
                JsonNode choices = output.path("choices");

                if (choices.isArray() && choices.size() > 0) {
                    JsonNode message = choices.get(0).path("message");
                    return message.path("content").asText();
                }
            }

            return "抱歉，生成学习建议时出现问题。";

        } catch (Exception e) {
            System.err.println("调用千问API失败: " + e.getMessage());
            e.printStackTrace();
            return "抱歉，生成学习建议时出现问题。请稍后重试。";
        }
    }

    /**
     * 构建学习建议的提示词
     */
    private String buildStudySuggestionPrompt(String statistics, String timeRange) {
        return String.format(
                "你是一个专业的学习顾问。根据以下学习统计数据，为用户提供个性化、详细的学习建议。\n\n" +
                        "时间范围: %s\n" +
                        "学习统计数据:\n%s\n\n" +
                        "请根据这些数据，生成3-4条详细、实用的学习建议。要求:\n" +
                        "1. 针对性强，基于数据说话\n" +
                        "2. 语气友好、鼓励性强\n" +
                        "3. 建议具体、可执行\n" +
                        "4. 每条建议150-200字左右\n" +
                        "5. 直接返回建议内容，不要有其他说明\n\n" +
                        "请只返回建议内容，每条建议用换行分隔。",
                timeRange, statistics);
    }

    /**
     * 异步调用千问API生成学习建议
     */
    @Override
    public CompletableFuture<String> generateStudySuggestionsAsync(String statistics, String timeRange) {
        return CompletableFuture.supplyAsync(() -> {
            String prompt = buildStudySuggestionPrompt(statistics, timeRange);
            return generateText(prompt);
        }, executorService);
    }

    /**
     * 异步调用千问API进行文本生成
     */
    @Override
    public CompletableFuture<String> generateTextAsync(String prompt) {
        return CompletableFuture.supplyAsync(() -> {
            return generateText(prompt);
        }, executorService);
    }
}
