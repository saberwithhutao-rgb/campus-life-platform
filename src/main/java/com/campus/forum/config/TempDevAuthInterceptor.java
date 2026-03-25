package com.campus.forum.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 临时开发用的认证拦截器
 * 识别请求头里 Token 为 "dev_fake_token_123" 的请求，直接放行
 * 解析这个伪造 Token，默认给 user_id = 1，不用做真实签名校验
 * 只在开发环境生效，生产环境恢复严格校验
 * 上线要删掉
 */
@Component
public class TempDevAuthInterceptor implements HandlerInterceptor {

    @Value("${spring.profiles.active:default}")
    private String activeProfile;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 只在开发环境生效
        if (!"dev".equals(activeProfile)) {
            return true;
        }

        String token = request.getHeader("Token");
        if ("dev_fake_token_123".equals(token)) {
            // 伪造 Token，默认给 user_id = 1
            request.setAttribute("user_id", 1L);
            return true;
        }

        // 其他情况继续处理（如果后续有真实的认证逻辑）
        return true;
    }
}
