package com.example.user.vo;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class StockFlowDailyStatsVO {
    private String date;
    private BigDecimal inbound;
    private BigDecimal outbound;
}
