package com.example.user.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 系统设置实体类
 */
@Data
public class SystemSetting {
    /**
     * 设置ID
     */
    private Long id;

    /**
     * 设置键
     */
    private String settingKey;

    /**
     * 设置值
     */
    private String settingValue;

    /**
     * 设置类型
     */
    private String settingType;

    /**
     * 描述
     */
    private String description;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}
