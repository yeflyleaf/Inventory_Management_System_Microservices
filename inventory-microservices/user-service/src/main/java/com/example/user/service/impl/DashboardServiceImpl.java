package com.example.user.service.impl;

import com.example.user.mapper.DashboardMapper;
import com.example.user.service.DashboardService;
import com.example.user.service.SystemSettingService;
import com.example.user.vo.*;
import com.example.user.feign.RemoteProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DashboardServiceImpl implements DashboardService {

    @Autowired
    private DashboardMapper dashboardMapper;

    @Autowired
    private SystemSettingService systemSettingService;

    @Autowired
    private RemoteProductService remoteProductService;

    @Override
    @Cacheable(value = "dashboardStats", key = "{#itemId, #days, #warehouseId, #category, #itemName}", unless = "#result == null")
    public DashboardVO getStats(Long itemId, Integer days, Long warehouseId, String category, String itemName) {
        DashboardVO vo = new DashboardVO();

        // 1. Today's Sales
        vo.setTodaySales(dashboardMapper.sumTodaySales());

        // 2. Stock Stats
        int totalStock = 0;
        int lowStockCount = 0;
        int LOW_STOCK_THRESHOLD = systemSettingService.getIntValue("low_stock_threshold", 10);

        // Fetch stock snapshot from Product Service
        List<Map<String, Object>> stocks = remoteProductService.getStockSnapshot().getData();
        if (stocks != null) {
            for (Map<String, Object> stock : stocks) {
                Integer currentStock = (Integer) stock.get("currentStock");
                if (currentStock == null) currentStock = 0;
                
                // Filter logic
                if (itemId != null && !itemId.equals(Long.valueOf(stock.get("itemId").toString()))) continue;
                
                totalStock += currentStock;
                if (currentStock < LOW_STOCK_THRESHOLD) {
                    lowStockCount++;
                }
            }
        }
        vo.setTotalStock(totalStock);
        vo.setLowStockCount(lowStockCount);

        // 3. Recent Activity
        List<Map<String, Object>> recentFlows = dashboardMapper.selectRecentFlows(5);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        if (recentFlows != null) {
            vo.setRecentActivity(recentFlows.stream().map(flow -> {
                DashboardActivityVO activity = new DashboardActivityVO();
                Object createdAt = flow.get("created_at");
                if (createdAt instanceof LocalDateTime) {
                    activity.setTime(((LocalDateTime) createdAt).format(formatter));
                } else {
                    activity.setTime(createdAt.toString());
                }
                String type = (String) flow.get("change_type");
                Integer amount = (Integer) flow.get("change_amount");
                activity.setDescription(type + " " + (amount > 0 ? "+" : "") + amount + " (Item #" + flow.get("item_id") + ")");
                return activity;
            }).collect(Collectors.toList()));
        }

        // 4. Stock Trend
        int daysToQuery = days != null ? days : 30;
        List<Map<String, Object>> trendData = dashboardMapper.selectDailyStats(daysToQuery, warehouseId, category, itemName);
        if (trendData != null) {
            vo.setStockTrend(trendData.stream().map(data -> {
                StockFlowDailyStatsVO stat = new StockFlowDailyStatsVO();
                stat.setDate(data.get("date").toString());
                stat.setInbound((BigDecimal) data.get("inbound"));
                stat.setOutbound((BigDecimal) data.get("outbound"));
                return stat;
            }).collect(Collectors.toList()));
        }

        // 5. Pending Tasks
        vo.setPendingPurchaseOrders(dashboardMapper.countPendingPurchaseOrders());
        vo.setPendingSalesOrders(dashboardMapper.countPendingSalesOrders());
        vo.setAbnormalOrders(0);

        // 6. Overstock
        int backlogDays = systemSettingService.getIntValue("inventory_backlog_days", 60);
        List<Long> activeItemIds = dashboardMapper.selectDistinctItemIdsRecent(backlogDays);
        if (stocks != null && itemId == null) {
            long overstock = stocks.stream()
                    .filter(s -> {
                        Integer cs = (Integer) s.get("currentStock");
                        return cs != null && cs > 0;
                    })
                    .filter(s -> {
                        Long sid = Long.valueOf(s.get("itemId").toString());
                        return activeItemIds == null || !activeItemIds.contains(sid);
                    })
                    .count();
            vo.setOverstockCount((int) overstock);
        } else {
            vo.setOverstockCount(0);
        }

        return vo;
    }

    @Override
    @Cacheable(value = "dashboardCategoryStats", key = "#type")
    public List<CategoryStatsVO> getCategoryStats(String type) {
        if ("SALES_QTY".equals(type)) {
            return mapToCategoryStats(dashboardMapper.selectSalesQtyByCategory());
        } else if ("SALES_AMOUNT".equals(type)) {
            return mapToCategoryStats(dashboardMapper.selectSalesAmountByCategory());
        } else {
            // Default to STOCK
            List<Map<String, Object>> stocks = remoteProductService.getStockSnapshot().getData();
            Map<String, Integer> categoryMap = new HashMap<>();
            if (stocks != null) {
                for (Map<String, Object> stock : stocks) {
                    String cat = stock.get("category") != null ? stock.get("category").toString() : "Uncategorized";
                    Integer cs = (Integer) stock.get("currentStock");
                    categoryMap.put(cat, categoryMap.getOrDefault(cat, 0) + (cs != null ? cs : 0));
                }
            }
            return categoryMap.entrySet().stream()
                    .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                    .limit(5)
                    .map(e -> {
                        CategoryStatsVO stat = new CategoryStatsVO();
                        stat.setCategory(e.getKey());
                        stat.setValue(new BigDecimal(e.getValue()));
                        return stat;
                    })
                    .collect(Collectors.toList());
        }
    }

    private List<CategoryStatsVO> mapToCategoryStats(List<Map<String, Object>> data) {
        if (data == null) return new ArrayList<>();
        return data.stream().map(m -> {
            CategoryStatsVO stat = new CategoryStatsVO();
            stat.setCategory(m.get("category").toString());
            stat.setValue((BigDecimal) m.get("value"));
            return stat;
        }).collect(Collectors.toList());
    }
}
