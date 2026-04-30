package com.example.order.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class SaleOrder {
    private Long id;
    private String orderNo;
    private Long customerId;
    private BigDecimal totalAmount;
    private String status;
    private Long createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
