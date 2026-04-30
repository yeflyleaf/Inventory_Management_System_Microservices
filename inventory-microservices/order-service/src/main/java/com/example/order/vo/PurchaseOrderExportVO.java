package com.example.order.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.format.DateTimeFormat;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 采购订单导出实体类
 */
@Data
public class PurchaseOrderExportVO {

    @ExcelProperty("订单ID")
    @ColumnWidth(15)
    private Long id;

    @ExcelProperty("订单编号")
    @ColumnWidth(25)
    private String orderNo;

    @ExcelProperty("供应商ID")
    @ColumnWidth(15)
    private Long supplierId;

    @ExcelProperty("总金额")
    @ColumnWidth(15)
    private BigDecimal totalAmount;

    @ExcelProperty("状态")
    @ColumnWidth(15)
    private String status;

    @ExcelProperty("创建时间")
    @ColumnWidth(25)
    @DateTimeFormat("yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}
