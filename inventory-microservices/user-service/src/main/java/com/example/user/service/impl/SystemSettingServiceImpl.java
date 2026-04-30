package com.example.user.service.impl;

import com.example.user.mapper.SystemSettingMapper;
import com.example.user.entity.SystemSetting;
import com.example.user.service.SystemSettingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 系统设置服务实现类
 */
@Service
public class SystemSettingServiceImpl implements SystemSettingService {
    
    @Autowired
    private SystemSettingMapper systemSettingMapper;
    
    /**
     * 获取所有系统设置
     */
    @Override
    @Cacheable(value = "systemSettings", key = "'all'")
    public List<SystemSetting> findAll() {
        return systemSettingMapper.findAll();
    }
    
    /**
     * 根据键获取设置值
     */
    @Override
    @Cacheable(value = "systemSettings", key = "#key")
    public String getValue(String key) {
        SystemSetting setting = systemSettingMapper.findByKey(key);
        return setting != null ? setting.getSettingValue() : null;
    }
    
    /**
     * 根据键获取设置值，如果不存在则返回默认值
     */
    @Override
    public String getValue(String key, String defaultValue) {
        String value = getValue(key);
        return value != null ? value : defaultValue;
    }
    
    /**
     * 获取整数类型的设置值
     */
    @Override
    public Integer getIntValue(String key, Integer defaultValue) {
        String value = getValue(key);
        if (value != null) {
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        }
        return defaultValue;
    }
    
    /**
     * 获取布尔类型的设置值
     */
    @Override
    public Boolean getBooleanValue(String key, Boolean defaultValue) {
        String value = getValue(key);
        if (value != null) {
            return "true".equalsIgnoreCase(value) || "1".equals(value);
        }
        return defaultValue;
    }
    
    /**
     * 更新或创建设置值
     */
    @Override
    @CacheEvict(value = "systemSettings", allEntries = true)
    public void updateValue(String key, String value) {
        SystemSetting existing = systemSettingMapper.findByKey(key);
        if (existing == null) {
            SystemSetting newSetting = new SystemSetting();
            newSetting.setSettingKey(key);
            newSetting.setSettingValue(value);
            newSetting.setSettingType("string"); 
            newSetting.setDescription("Auto created setting");
            systemSettingMapper.insert(newSetting);
        } else {
            systemSettingMapper.updateValue(key, value);
        }
    }
    
    /**
     * 批量更新设置
     */
    @Override
    @CacheEvict(value = "systemSettings", allEntries = true)
    public void batchUpdate(Map<String, String> settings) {
        for (Map.Entry<String, String> entry : settings.entrySet()) {
            updateValue(entry.getKey(), entry.getValue());
        }
    }
    
    /**
     * 获取所有设置并转换为Map格式
     */
    @Override
    @Cacheable(value = "systemSettings", key = "'map'")
    public Map<String, String> getAllAsMap() {
        List<SystemSetting> settings = findAll();
        Map<String, String> map = new HashMap<>();
        for (SystemSetting setting : settings) {
            map.put(setting.getSettingKey(), setting.getSettingValue());
        }
        return map;
    }
}
