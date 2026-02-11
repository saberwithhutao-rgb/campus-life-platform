package com.school.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 千问API配置类
 */
@Configuration
@ConfigurationProperties(prefix = "qianwen.api")
public class QianwenConfig {
    
    /**
     * API密钥
     */
    private String apiKey;
    
    /**
     * API端点
     */
    private String endpoint = "https://dashscope.aliyuncs.com/api/v1/services/aigc/text-generation/generation";
    
    /**
     * 模型名称
     */
    private String model = "qwen-max";
    
    /**
     * 超时时间（秒）
     */
    private Integer timeout = 30;

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public Integer getTimeout() {
        return timeout;
    }

    public void setTimeout(Integer timeout) {
        this.timeout = timeout;
    }
}
