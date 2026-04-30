package com.example.user.service;

import com.example.common.entity.OperationLog;
import com.example.common.domain.PageResult;
import java.time.LocalDateTime;
import java.util.List;

public interface OperationLogService {
    void log(OperationLog log);
    PageResult<OperationLog> findByPage(Long userId, String username, String module, 
                                         String action, LocalDateTime startTime, 
                                         LocalDateTime endTime, int page, int size);
    List<String> findAllModules();
    int cleanOldLogs(int keepDays);
    int countToday();
    List<OperationLog> findRecent(int limit);
}
