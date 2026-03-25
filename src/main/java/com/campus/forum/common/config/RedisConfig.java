package com.campus.forum.common.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Redis 配置类 - 高并发预约模块专用
 * 配置 RedisTemplate，设置序列化方式和连接池
 */
@Configuration
public class RedisConfig {

    /**
     * 配置 RedisTemplate
     * 使用 StringRedisSerializer 序列化键
     * 使用 GenericJackson2JsonRedisSerializer 序列化值
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory, ObjectMapper objectMapper) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // 设置键序列化方式为 String
        template.setKeySerializer(new StringRedisSerializer());
        // 设置哈希键序列化方式为 String
        template.setHashKeySerializer(new StringRedisSerializer());
        // 设置值序列化方式为 Jackson JSON，使用配置好的 ObjectMapper
        Jackson2JsonRedisSerializer<Object> serializer = new Jackson2JsonRedisSerializer<>(objectMapper, Object.class);
        template.setValueSerializer(serializer);
        // 设置哈希值序列化方式为 Jackson JSON，使用配置好的 ObjectMapper
        template.setHashValueSerializer(serializer);

        template.afterPropertiesSet();
        return template;
    }
}
