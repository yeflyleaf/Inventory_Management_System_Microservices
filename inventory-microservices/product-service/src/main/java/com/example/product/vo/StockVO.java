package com.example.product.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class StockVO {
    private Long itemId;
    private String itemName;
    private String itemSku;
    private String category;
    private Long warehouseId;
    private String warehouseName;
    private Integer currentStock;
    private LocalDateTime lastMovementDate;
}
