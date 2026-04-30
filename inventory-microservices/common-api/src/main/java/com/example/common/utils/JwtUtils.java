package com.example.common.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
public class JwtUtils {
    
    @Autowired
    private RedisUtils redisUtils;
    
    private static final String TOKEN_PREFIX = "token:";
    private static final long TOKEN_EXPIRE_TIME = 7 * 24 * 60 * 60;

    public String generateToken(Long userId, String username) {
        return generateToken(userId, username, "USER");
    }
    
    public String generateToken(Long userId, String username, String role) {
        String token = UUID.randomUUID().toString().replace("-", "");
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("userId", userId);
        userInfo.put("username", username);
        userInfo.put("role", role);
        userInfo.put("createTime", System.currentTimeMillis());
        String redisKey = TOKEN_PREFIX + token;
        redisUtils.set(redisKey, userInfo, TOKEN_EXPIRE_TIME);
        return token;
    }

    public boolean validateToken(String token) {
        if (token == null || token.isEmpty()) {
            return false;
        }
        String redisKey = TOKEN_PREFIX + token;
        return redisUtils.hasKey(redisKey);
    }

    public Long getUserIdFromToken(String token) {
        try {
            String redisKey = TOKEN_PREFIX + token;
            Object userInfoObj = redisUtils.get(redisKey);
            if (userInfoObj instanceof Map) {
                Map<String, Object> userInfo = (Map<String, Object>) userInfoObj;
                Object userIdObj = userInfo.get("userId");
                if (userIdObj instanceof Number) {
                    return ((Number) userIdObj).longValue();
                }
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }
    
    public String getUsernameFromToken(String token) {
        try {
            String redisKey = TOKEN_PREFIX + token;
            Object userInfoObj = redisUtils.get(redisKey);
            if (userInfoObj instanceof Map) {
                Map<String, Object> userInfo = (Map<String, Object>) userInfoObj;
                return (String) userInfo.get("username");
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }
    
    public void removeToken(String token) {
        if (token != null && !token.isEmpty()) {
            String redisKey = TOKEN_PREFIX + token;
            redisUtils.del(redisKey);
        }
    }
    
    public String getRoleFromToken(String token) {
        try {
            String redisKey = TOKEN_PREFIX + token;
            Object userInfoObj = redisUtils.get(redisKey);
            if (userInfoObj instanceof Map) {
                Map<String, Object> userInfo = (Map<String, Object>) userInfoObj;
                return (String) userInfo.get("role");
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }
    
    public boolean isAdmin(String token) {
        String role = getRoleFromToken(token);
        return "ADMIN".equalsIgnoreCase(role);
    }
}
