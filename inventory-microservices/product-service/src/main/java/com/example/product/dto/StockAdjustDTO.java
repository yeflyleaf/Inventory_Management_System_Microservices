package com.example.product.dto;

import lombok.Data;

@Data
public class StockAdjustDTO {
    private Long itemId;
    private Integer changeAmount;
    private String changeType;
    private Long warehouseId;
    private String description;
}
