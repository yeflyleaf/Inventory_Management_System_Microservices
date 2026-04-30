package com.example.user.service.impl;

import com.example.user.mapper.OperationLogMapper;
import com.example.common.entity.OperationLog;
import com.example.user.service.OperationLogService;
import com.example.common.domain.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class OperationLogServiceImpl implements OperationLogService {
    
    @Autowired
    private OperationLogMapper operationLogMapper;
    
    @Override
    @Async
    public void log(OperationLog log) {
        if (log.getCreatedAt() == null) {
            log.setCreatedAt(LocalDateTime.now());
        }
        operationLogMapper.insert(log);
    }
    
    @Override
    public PageResult<OperationLog> findByPage(Long userId, String username, String module,
                                                String action, LocalDateTime startTime,
                                                LocalDateTime endTime, int page, int size) {
        int offset = (page - 1) * size;
        List<OperationLog> logs = operationLogMapper.findByCondition(userId, username, module, 
                                                                      action, startTime, endTime, 
                                                                      offset, size);
        int total = operationLogMapper.countByCondition(userId, username, module, 
                                                         action, startTime, endTime);
        return new PageResult<>(logs, total, page, size);
    }
    
    @Override
    public List<String> findAllModules() { return operationLogMapper.findAllModules(); }
    
    @Override 
    public int cleanOldLogs(int keepDays) { 
        LocalDateTime before = LocalDateTime.now().minusDays(keepDays);
        // Implementation would call mapper to delete
        return 0; 
    }
    
    @Override 
    public int countToday() { 
        LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
        return operationLogMapper.countByCondition(null, null, null, null, startOfDay, null);
    }
    
    @Override 
    public List<OperationLog> findRecent(int limit) { 
        return operationLogMapper.findByCondition(null, null, null, null, null, null, 0, limit);
    }
}

