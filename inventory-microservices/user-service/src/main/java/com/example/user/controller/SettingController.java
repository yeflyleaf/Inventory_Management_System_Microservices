package com.example.user.controller;

import com.example.common.annotation.Log;
import com.example.common.domain.Result;
import com.example.common.entity.OperationLog;
import com.example.user.entity.SystemSetting;
import com.example.user.entity.User;
import com.example.user.service.OperationLogService;
import com.example.user.service.SystemSettingService;
import com.example.user.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 系统设置控制器
 */
@RestController
@RefreshScope
@RequestMapping("/admin/settings")
@CrossOrigin
public class SettingController {

    @Autowired
    private SystemSettingService systemSettingService;

    @Autowired
    private UserService userService;

    @Autowired
    private OperationLogService operationLogService;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 获取所有系统设置
     */
    @GetMapping
    public Result<List<SystemSetting>> getAllSettings() {
        return Result.success(systemSettingService.findAll());
    }

    /**
     * 获取系统设置（Map形式）
     */
    @GetMapping("/map")
    public Result<Map<String, String>> getSettingsAsMap() {
        return Result.success(systemSettingService.getAllAsMap());
    }

    /**
     * 根据键获取设置值
     */
    @GetMapping("/value")
    public Result<String> getSettingValue(@RequestParam("key") String key, 
                                         @RequestParam(value = "defaultValue", required = false) String defaultValue) {
        return Result.success(systemSettingService.getValue(key, defaultValue));
    }

    /**
     * 更新单个设置
     */
    @PutMapping("/{key}")
    @Log(module = "系统设置", action = "更新设置", description = "更新单个系统设置")
    public Result<Void> updateSetting(@PathVariable String key, @RequestBody Map<String, String> body) {
        String value = body.get("value");
        systemSettingService.updateValue(key, value);
        return Result.success(null, "设置已更新");
    }

    /**
     * 批量更新设置
     */
    @PutMapping
    public Result<Void> batchUpdateSettings(@RequestBody Map<String, String> settings, HttpServletRequest request) {
        // 获取当前设置用于对比
        Map<String, String> currentSettings = systemSettingService.getAllAsMap();
        
        // 定义日志动作映射
        Map<String, String> actionMap = Map.of(
            "company_name", "修改公司名称",
            "company_phone", "修改联系电话",
            "company_email", "修改公司邮箱",
            "company_address", "修改公司地址",
            "low_stock_threshold", "修改低库存预警阈值",
            "inventory_backlog_days", "修改库存积压天数",
            "allow_negative_stock", "修改允许负库存",
            "order_prefix_purchase", "修改采购单编号前缀",
            "order_prefix_sales", "修改销售单编号前缀"
        );

        Long userId = null;
        String username = null;
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            com.example.common.utils.JwtUtils jwtUtils = org.springframework.web.context.support.WebApplicationContextUtils.getRequiredWebApplicationContext(request.getServletContext()).getBean(com.example.common.utils.JwtUtils.class);
            userId = jwtUtils.getUserIdFromToken(token);
            username = jwtUtils.getUsernameFromToken(token);
        }

        for (Map.Entry<String, String> entry : settings.entrySet()) {
            String key = entry.getKey();
            String newValue = entry.getValue();
            String oldValue = currentSettings.get(key);

            // 对比值是否发生变化
            if (!Objects.equals(oldValue, newValue)) {
                String action = actionMap.getOrDefault(key, "修改系统设置");
                String description = String.format("%s: 从 '%s' 修改为 '%s'", action, oldValue == null ? "" : oldValue, newValue);
                
                // 创建日志
                OperationLog log = new OperationLog();
                log.setModule("系统设置");
                log.setAction(action);
                log.setDescription(description);
                log.setUserId(userId);
                log.setUsername(username);
                log.setRequestMethod(request.getMethod());
                log.setRequestUrl(request.getRequestURI());
                log.setResponseStatus(200);
                log.setCreatedAt(LocalDateTime.now());
                log.setExecutionTime(0);
                
                try {
                    // 记录变更的参数
                    Map<String, String> param = Map.of(key, newValue);
                    log.setRequestParams(objectMapper.writeValueAsString(param));
                } catch (Exception e) {
                    log.setRequestParams(key + "=" + newValue);
                }

                operationLogService.log(log);
            }
        }

        systemSettingService.batchUpdate(settings);
        return Result.success(null, "设置已批量更新");
    }
}

