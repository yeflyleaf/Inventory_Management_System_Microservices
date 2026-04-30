package com.example.user.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardVO {
    private BigDecimal todaySales;
    private Integer totalStock;
    private Integer lowStockCount;
    private List<DashboardActivityVO> recentActivity;
    private List<StockFlowDailyStatsVO> stockTrend;
    private Integer pendingPurchaseOrders;
    private Integer pendingSalesOrders;
    private Integer abnormalOrders;
    private Integer overstockCount;
}
