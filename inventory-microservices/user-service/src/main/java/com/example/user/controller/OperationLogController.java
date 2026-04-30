package com.example.user.controller;

import com.example.common.annotation.Log;
import com.example.common.domain.PageResult;
import com.example.common.domain.Result;
import com.example.user.dto.LogQueryDTO;
import com.example.common.entity.OperationLog;
import com.example.user.service.OperationLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 操作日志控制器
 */
@RestController
@RequestMapping("/admin/logs")
@CrossOrigin
public class OperationLogController {

    @Autowired
    private OperationLogService operationLogService;

    /**
     * 查询操作日志
     */
    @GetMapping
    public Result<PageResult<OperationLog>> getLogs(LogQueryDTO queryDTO) {
        LocalDateTime startTime = null;
        LocalDateTime endTime = null;
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        if (queryDTO.getStartTime() != null && !queryDTO.getStartTime().isEmpty()) {
            startTime = LocalDateTime.parse(queryDTO.getStartTime(), formatter);
        }
        if (queryDTO.getEndTime() != null && !queryDTO.getEndTime().isEmpty()) {
            endTime = LocalDateTime.parse(queryDTO.getEndTime(), formatter);
        }
        
        PageResult<OperationLog> result = operationLogService.findByPage(
            queryDTO.getUserId(),
            queryDTO.getUsername(),
            queryDTO.getModule(),
            queryDTO.getAction(),
            startTime,
            endTime,
            queryDTO.getPage(),
            queryDTO.getSize()
        );
        
        return Result.success(result);
    }
    
    /**
     * 获取最近操作日志
     */
    @GetMapping("/recent")
    public Result<List<OperationLog>> getRecentLogs(@RequestParam(defaultValue = "10") int limit) {
        return Result.success(operationLogService.findRecent(limit));
    }
    
    /**
     * 获取所有日志模块
     */
    @GetMapping("/modules")
    public Result<List<String>> getLogModules() {
        return Result.success(operationLogService.findAllModules());
    }
    
    /**
     * 清理旧日志
     */
    @DeleteMapping("/clean")
    @Log(module = "系统管理", action = "清理日志", description = "清理旧操作日志")
    public Result<Integer> cleanOldLogs(@RequestParam(defaultValue = "90") int keepDays) {
        int count = operationLogService.cleanOldLogs(keepDays);
        return Result.success(count, "已清理 " + count + " 条旧日志");
    }

    /**
     * 记录日志 (供内部微服务调用)
     */
    @PostMapping("/internal/save")
    public Result<Void> saveLog(@RequestBody OperationLog operationLog) {
        operationLogService.log(operationLog);
        return Result.success(null);
    }
}
