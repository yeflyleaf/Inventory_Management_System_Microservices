package com.example.user.feign;

import com.example.common.domain.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@FeignClient(name = "order-service")
public interface RemoteOrderService {

    @GetMapping("/purchase-orders/count/created")
    Result<Integer> countPendingPurchaseOrders();

    @GetMapping("/sales-orders/count/created")
    Result<Integer> countPendingSalesOrders();

    @GetMapping("/sales-orders/stats/today-sales")
    Result<BigDecimal> sumTodaySales();

    @GetMapping("/sales-orders/stats/category-qty")
    Result<List<Map<String, Object>>> getSalesQtyByCategory();

    @GetMapping("/sales-orders/stats/category-amount")
    Result<List<Map<String, Object>>> getSalesAmountByCategory();
}
