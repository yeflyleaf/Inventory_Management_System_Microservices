package com.example.user.vo;

import lombok.Data;

@Data
public class LoginVO {
    private Long id;
    private String username;
    private String nickname;
    private String role;
    private String token;
    private String avatar;
    private String email;
    private String phone;
}
