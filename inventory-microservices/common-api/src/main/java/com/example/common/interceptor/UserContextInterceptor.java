package com.example.common.interceptor;

import com.example.common.utils.UserContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 用户上下文拦截器
 * 从 Header 中提取用户信息并放入 UserContext
 */
public class UserContextInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String userIdStr = request.getHeader("X-User-Id");
        String username = request.getHeader("X-User-Name");
        String role = request.getHeader("X-User-Role");

        if (userIdStr != null) {
            UserContext.setUserId(Long.valueOf(userIdStr));
        }
        if (username != null) {
            UserContext.setUsername(username);
        }
        if (role != null) {
            UserContext.setRole(role);
        }

        // 兼容原有的 request.setAttribute("userId") 逻辑
        if (userIdStr != null) {
            request.setAttribute("userId", Long.valueOf(userIdStr));
        }

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        UserContext.clear();
    }
}
