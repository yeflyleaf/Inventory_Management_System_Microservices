package com.example.common.vo;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 客户视图对象
 */
@Data
public class CustomerVO {
    private Long id;
    private String name;
    private String contactPerson;
    private String phone;
    private String phone2;
    private String address;
    private LocalDateTime createdAt;
}
