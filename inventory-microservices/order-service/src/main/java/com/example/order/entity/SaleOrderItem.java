package com.example.order.entity;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class SaleOrderItem {
    private Long id;
    private Long salesOrderId;
    private Long itemId;
    private Integer qty;
    private BigDecimal salePrice;
    private BigDecimal subtotal;
}
