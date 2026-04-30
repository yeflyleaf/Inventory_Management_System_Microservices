package com.example.common.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 商品信息视图对象
 */
@Data
public class ProductVO {
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
