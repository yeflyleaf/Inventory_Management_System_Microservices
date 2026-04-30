package com.example.user.mapper;

import com.example.user.entity.SystemSetting;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * 系统设置数据访问接口
 */
@Mapper
public interface SystemSettingMapper {
    /**
     * 获取所有设置
     */
    List<SystemSetting> findAll();
    
    /**
     * 根据键获取设置
     */
    SystemSetting findByKey(String key);
    
    /**
     * 更新设置值
     */
    int updateValue(@Param("key") String key, @Param("value") String value);

    /**
     * 插入新设置
     */
    int insert(SystemSetting systemSetting);

    /**
     * 删除设置
     */
    int deleteByKey(String key);
}
