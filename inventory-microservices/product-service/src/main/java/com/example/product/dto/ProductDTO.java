package com.example.product.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class ProductDTO {
    private Long id;
    private String sku;
    private String name;
    private String category;
    private String unit;
    private String barcode;
    private BigDecimal salePrice;
    private List<String> imageUrls;
}
