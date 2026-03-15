package com.campus.forum.common.interceptor;

import com.campus.forum.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class JwtInterceptor implements HandlerInterceptor {
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String authHeader = request.getHeader("Authorization");
        Integer userId = extractUserIdFromToken(authHeader);
        request.setAttribute("userId", userId);
        return true;
    }
    
    private Integer extractUserIdFromToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("未提供Token或Token格式错误");
        }
        String token = authHeader.substring(7);
        try {
            Long userIdLong = jwtUtil.getUserIdFromToken(token);
            if (userIdLong == null) throw new RuntimeException("Token中不存在userId");
            return userIdLong.intValue();
        } catch (Exception e) {
            throw new RuntimeException("无效的Token", e);
        }
    }
}