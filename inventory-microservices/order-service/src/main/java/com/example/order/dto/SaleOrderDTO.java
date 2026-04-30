package com.example.order.dto;

import lombok.Data;
import java.util.List;

@Data
public class SaleOrderDTO {
    private Long customerId;
    private List<SaleOrderItemDTO> items;
}
