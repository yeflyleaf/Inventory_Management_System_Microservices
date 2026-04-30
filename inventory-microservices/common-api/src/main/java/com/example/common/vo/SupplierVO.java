package com.example.common.vo;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 供应商视图对象
 */
@Data
public class SupplierVO {
    private Long id;
    private String name;
    private String contactPerson;
    private String phone;
    private String phone2;
    private String address;
    private LocalDateTime createdAt;
}
