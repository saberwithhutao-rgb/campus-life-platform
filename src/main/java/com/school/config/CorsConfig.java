package com.school.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * CORS 跨域配置类
 * 解决前端浏览器跨域访问问题
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // 允许所有路径的跨域请求
        registry.addMapping("/**")
                // 允许的域名模式，使用 allowedOriginPatterns 替代 allowedOrigins
                .allowedOriginPatterns("*")
                // 允许的HTTP方法
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                // 允许的请求头
                .allowedHeaders("*")
                // 是否允许携带凭证（如cookie）
                .allowCredentials(true)
                // 预检请求的有效期，单位秒
                .maxAge(3600);
    }
}
