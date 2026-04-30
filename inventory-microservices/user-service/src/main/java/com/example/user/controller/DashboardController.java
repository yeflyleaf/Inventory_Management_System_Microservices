package com.example.user.controller;

import com.example.common.domain.Result;
import com.example.user.service.DashboardService;
import com.example.user.vo.DashboardVO;
import com.example.user.vo.CategoryStatsVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * 仪表盘控制器
 */
@RestController
@RequestMapping("/dashboard")
@CrossOrigin
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    @GetMapping("/stats")
    public Result<DashboardVO> getStats(
            @RequestParam(required = false) Long itemId,
            @RequestParam(required = false) Integer days,
            @RequestParam(required = false) Long warehouseId,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String itemName
    ) {
        DashboardVO stats = dashboardService.getStats(itemId, days, warehouseId, category, itemName);
        return Result.success(stats, "获取仪表盘数据成功");
    }

    @GetMapping("/category-stats")
    public Result<List<CategoryStatsVO>> getCategoryStats(@RequestParam(defaultValue = "STOCK") String type) {
        return Result.success(dashboardService.getCategoryStats(type), "Success");
    }
}
