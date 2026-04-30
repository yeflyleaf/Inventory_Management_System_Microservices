package com.example.order.feign;

import com.example.common.domain.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "user-service")
public interface RemoteUserService {

    @GetMapping("/admin/settings/value")
    Result<String> getSettingValue(@RequestParam("key") String key, @RequestParam("defaultValue") String defaultValue);

    @PostMapping("/admin/logs/internal/save")
    Result<Void> saveLog(@RequestBody com.example.common.entity.OperationLog operationLog);
}
