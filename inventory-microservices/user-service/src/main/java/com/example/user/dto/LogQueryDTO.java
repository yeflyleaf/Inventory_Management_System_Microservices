package com.example.user.dto;

import lombok.Data;

@Data
public class LogQueryDTO {
    private Long userId;
    private String username;
    private String module;
    private String action;
    private String startTime;
    private String endTime;
    private int page = 1;
    private int size = 10;
}
