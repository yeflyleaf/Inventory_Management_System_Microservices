package com.example.order.feign;

import com.example.common.domain.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "product-service", contextId = "stockFeignClient", fallback = com.example.order.feign.fallback.StockFeignClientFallback.class)
public interface StockFeignClient {

    @PostMapping("/stock/adjust-internal")
    Result<Void> adjustStock(@RequestParam("itemId") Long itemId,
                             @RequestParam("changeAmount") Integer changeAmount,
                             @RequestParam("changeType") String changeType,
                             @RequestParam("warehouseId") Long warehouseId,
                             @RequestParam("description") String description);
}
