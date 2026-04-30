package com.example.product.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class StockFlow {
    private Long id;
    private Long itemId;
    private Integer changeAmount;
    private String changeType;
    private String refType;
    private String refId;
    private Long warehouseId;
    private LocalDateTime createdAt;
}
