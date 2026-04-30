package com.example.user.service;

import com.example.user.entity.SystemSetting;
import java.util.List;
import java.util.Map;

/**
 * 系统设置服务接口
 */
public interface SystemSettingService {
    List<SystemSetting> findAll();
    
    String getValue(String key);
    
    String getValue(String key, String defaultValue);
    
    Integer getIntValue(String key, Integer defaultValue);
    
    Boolean getBooleanValue(String key, Boolean defaultValue);
    
    void updateValue(String key, String value);
    
    void batchUpdate(Map<String, String> settings);
    
    Map<String, String> getAllAsMap();
}
