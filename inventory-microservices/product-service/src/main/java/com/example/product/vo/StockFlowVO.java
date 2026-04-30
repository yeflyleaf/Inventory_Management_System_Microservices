package com.example.product.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class StockFlowVO {
    private Long id;
    private Long itemId;
    private String itemName;
    private Integer changeAmount;
    private String changeType;
    private String refType;
    private String refId;
    private Long warehouseId;
    private String warehouseName;
    private LocalDateTime createdAt;
}
