package com.campus.forum.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 从token中解析用户ID
     * @param token JWT token
     * @return 用户ID
     */
    public Integer getUserIdFromToken(String token) {
        if (token == null || token.isEmpty()) {
            throw new IllegalArgumentException("Token不能为空");
        }

        try {
            Claims claims = Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            Object userId = claims.get("userId");
            if (userId instanceof Integer) {
                return (Integer) userId;
            } else if (userId instanceof Long) {
                return ((Long) userId).intValue();
            } else if (userId instanceof String) {
                return Integer.parseInt((String) userId);
            }

            throw new IllegalArgumentException("无法从token中解析用户ID");
        } catch (Exception e) {
            throw new IllegalArgumentException("Token解析失败：" + e.getMessage());
        }
    }

    /**
     * 从Authorization header中提取token
     * @param authorization Authorization header值
     * @return token字符串
     */
    public String extractToken(String authorization) {
        if (authorization == null || authorization.isEmpty()) {
            return null;
        }
        
        if (authorization.startsWith("Bearer ")) {
            return authorization.substring(7);
        }
        
        return authorization;
    }
}
