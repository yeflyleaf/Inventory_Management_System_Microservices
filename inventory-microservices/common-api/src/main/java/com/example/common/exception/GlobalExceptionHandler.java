package com.example.common.exception;

import com.example.common.domain.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class GlobalExceptionHandler {


    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 处理自定义业务异常 (RuntimeException)
     */
    @ExceptionHandler(RuntimeException.class)
    public Result<String> handleRuntimeException(RuntimeException e) {
        logger.warn("捕获到业务异常: {}", e.getMessage());
        return Result.error(500, e.getMessage() != null ? e.getMessage() : "业务处理异常");
    }

    /**
     * 处理数据库唯一性约束冲突等数据异常
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public Result<String> handleDataIntegrityViolationException(DataIntegrityViolationException e) {
        logger.error("数据库约束异常: ", e);
        if (e.getMessage() != null && e.getMessage().contains("Duplicate entry")) {
            return Result.error(500, "记录已存在，请检查名称或编码是否重复");
        }
        return Result.error(500, "数据库操作失败，请检查数据准确性");
    }

    /**
     * 处理参数校验异常 (@RequestBody 参数校验失败)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<String> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        BindingResult bindingResult = e.getBindingResult();
        String message = bindingResult.getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        logger.warn("参数校验异常: {}", message);
        return Result.error(400, "参数验证失败: " + message);
    }

    /**
     * 处理参数绑定异常 (表单提交等)
     */
    @ExceptionHandler(BindException.class)
    public Result<String> handleBindException(BindException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        logger.warn("参数绑定异常: {}", message);
        return Result.error(400, "数据格式错误: " + message);
    }

    /**
     * 处理 HTTP 请求方法不支持异常
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public Result<String> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException e) {
        logger.warn("请求方法不支持: {}", e.getMethod());
        return Result.error(405, "请求方法不支持: " + e.getMethod());
    }

    /**
     * 处理兜底的系统异常
     */
    @ExceptionHandler(Exception.class)
    public Result<String> handleException(Exception e) {
        logger.error("系统未捕获异常: ", e);
        // 如果异常有消息，则尝试返回该消息，否则返回通用提示
        String msg = e.getMessage();
        if (msg == null || msg.trim().isEmpty()) {
            msg = "后端系统异常 ( " + e.getClass().getSimpleName() + " )";
        }
        return Result.error(500, msg);
    }
}


