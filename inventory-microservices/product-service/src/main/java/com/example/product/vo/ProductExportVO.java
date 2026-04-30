package com.example.product.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ProductExportVO {
    @ExcelProperty("商品ID")
    private Long id;

    @ExcelProperty("商品编码")
    private String sku;

    @ExcelProperty("商品名称")
    private String name;

    @ExcelProperty("分类")
    private String category;

    @ExcelProperty("单位")
    private String unit;

    @ExcelProperty("销售价")
    private BigDecimal salePrice;

    @ExcelProperty("创建时间")
    private LocalDateTime createdAt;
}
