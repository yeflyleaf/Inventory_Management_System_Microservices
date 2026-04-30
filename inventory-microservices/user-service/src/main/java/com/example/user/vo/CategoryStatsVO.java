package com.example.user.vo;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class CategoryStatsVO {
    private String category;
    private BigDecimal value;
}
