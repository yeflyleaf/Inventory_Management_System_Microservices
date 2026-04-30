package com.example.user.feign;

import com.example.common.domain.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.List;
import java.util.Map;

@FeignClient(name = "product-service")
public interface RemoteProductService {

    @GetMapping("/stock/snapshot/all")
    Result<List<Map<String, Object>>> getStockSnapshot();

    @GetMapping("/stock/flows/recent")
    Result<List<Map<String, Object>>> getRecentFlows(@RequestParam("limit") Integer limit);

    @GetMapping("/stock/stats/daily-trend")
    Result<List<Map<String, Object>>> getStockTrend(@RequestParam("days") Integer days,
                                                 @RequestParam(value = "warehouseId", required = false) Long warehouseId,
                                                 @RequestParam(value = "category", required = false) String category,
                                                 @RequestParam(value = "itemName", required = false) String itemName);

    @GetMapping("/stock/flows/distinct-item-ids")
    Result<List<Long>> getDistinctItemIdsRecent(@RequestParam("days") Integer days);
}
