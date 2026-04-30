package com.example.user.dto;

import lombok.Data;

@Data
public class UserAddDTO {
    private Long id;
    private String username;
    private String password;
    private String nickname;
    private String role;
    private Integer status;
    private String avatar;
    private String phone;
    private String email;
}
