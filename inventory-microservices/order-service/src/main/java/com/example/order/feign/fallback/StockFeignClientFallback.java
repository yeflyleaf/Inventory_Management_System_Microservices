package com.example.order.feign.fallback;

import com.example.common.domain.Result;
import com.example.order.feign.StockFeignClient;
import org.springframework.stereotype.Component;

@Component
public class StockFeignClientFallback implements StockFeignClient {
    @Override
    public Result<Void> adjustStock(Long itemId, Integer changeAmount, String changeType, Long warehouseId, String description) {
        return Result.error(503, "库存服务暂时不可用，请稍后再试 (Feign Fallback)");
    }
}
