package com.example.product.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class Product {
    private Long id;
    private String sku;
    private String name;
    private String category;
    private String unit;
    private String barcode;
    private BigDecimal salePrice;
    private List<String> imageUrls;
    private LocalDateTime createdAt;
}
