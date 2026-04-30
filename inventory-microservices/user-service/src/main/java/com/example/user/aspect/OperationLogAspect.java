package com.example.user.aspect;

import com.example.common.annotation.Log;
import com.example.common.entity.OperationLog;
import com.example.user.entity.User;
import com.example.user.service.OperationLogService;
import com.example.user.service.UserService;
import com.example.user.vo.LoginVO;
import com.example.common.domain.Result;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

/**
 * 操作日志切面
 */
@Aspect
@Component
public class OperationLogAspect {

    @Autowired
    private OperationLogService operationLogService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private ObjectMapper objectMapper;

    @Pointcut("@annotation(com.example.common.annotation.Log)")
    public void logPointCut() {}

    @Around("logPointCut()")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        long startTime = System.currentTimeMillis();
        Object result = null;
        Exception exception = null;
        
        try {
            result = point.proceed();
        } catch (Exception e) {
            exception = e;
            throw e;
        } finally {
            long time = System.currentTimeMillis() - startTime;
            saveLog(point, result, exception, time);
        }
        
        return result;
    }

    private void saveLog(ProceedingJoinPoint point, Object result, Exception exception, long time) {
        try {
            MethodSignature signature = (MethodSignature) point.getSignature();
            Method method = signature.getMethod();
            Log logAnnotation = method.getAnnotation(Log.class);
            
            OperationLog operationLog = new OperationLog();
            if (logAnnotation != null) {
                operationLog.setModule(logAnnotation.module());
                operationLog.setAction(logAnnotation.action());
                operationLog.setDescription(logAnnotation.description());
            }

            // 获取请求信息
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                operationLog.setUserAgent(request.getHeader("User-Agent"));
                operationLog.setRequestMethod(request.getMethod());
                operationLog.setRequestUrl(request.getRequestURI());
                
                // 从Token中获取用户信息
                String token = request.getHeader("Authorization");
                if (token != null && token.startsWith("Bearer ")) {
                    token = token.substring(7);
                    com.example.common.utils.JwtUtils jwtUtils = org.springframework.web.context.support.WebApplicationContextUtils.getRequiredWebApplicationContext(request.getServletContext()).getBean(com.example.common.utils.JwtUtils.class);
                    Long userId = jwtUtils.getUserIdFromToken(token);
                    String username = jwtUtils.getUsernameFromToken(token);
                    if (userId != null) {
                        operationLog.setUserId(userId);
                        operationLog.setUsername(username);
                    }
                }
            }

            // 特殊处理登录接口，从返回结果中获取用户信息
            if (operationLog.getUserId() == null && result != null && result instanceof Result) {
                Object data = ((Result<?>) result).getData();
                if (data instanceof LoginVO) {
                    LoginVO loginVO = (LoginVO) data;
                    operationLog.setUserId(loginVO.getId());
                    operationLog.setUsername(loginVO.getUsername());
                }
            }

            // 请求参数
            Object[] args = point.getArgs();
            try {
                String params = objectMapper.writeValueAsString(args);
                if (params.length() > 2000) {
                    params = params.substring(0, 2000) + "...";
                }
                operationLog.setRequestParams(params);
            } catch (Exception e) {
                // 忽略序列化错误
            }

            // 响应状态
            if (exception != null) {
                operationLog.setResponseStatus(500);
                String desc = operationLog.getDescription() == null ? "" : operationLog.getDescription();
                operationLog.setDescription(desc + " (Error: " + exception.getMessage() + ")");
            } else {
                operationLog.setResponseStatus(200);
            }
            
            operationLog.setExecutionTime((int) time);
            operationLog.setCreatedAt(LocalDateTime.now());
            
            operationLogService.log(operationLog);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
