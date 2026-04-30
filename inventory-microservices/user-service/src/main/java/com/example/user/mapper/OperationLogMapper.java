package com.example.user.mapper;

import com.example.common.entity.OperationLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface OperationLogMapper {
    int insert(OperationLog log);

    List<OperationLog> findByCondition(@Param("userId") Long userId,
                                        @Param("username") String username,
                                        @Param("module") String module,
                                        @Param("action") String action,
                                        @Param("startTime") LocalDateTime startTime,
                                        @Param("endTime") LocalDateTime endTime,
                                        @Param("offset") int offset,
                                        @Param("size") int size);
    int countByCondition(@Param("userId") Long userId,
                         @Param("username") String username,
                         @Param("module") String module,
                         @Param("action") String action,
                         @Param("startTime") LocalDateTime startTime,
                         @Param("endTime") LocalDateTime endTime);
    List<String> findAllModules();
}
