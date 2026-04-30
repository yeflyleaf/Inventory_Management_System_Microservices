package com.example.user.controller;

import com.example.common.domain.Result;
import com.example.common.annotation.Log;
import com.example.user.dto.LoginDTO;
import com.example.user.dto.UserAddDTO;
import com.example.user.vo.LoginVO;
import com.example.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@CrossOrigin
public class AuthController {

    @Autowired
    private UserService userService;

    @PostMapping("/login")
    @Log(module = "认证", action = "登录", description = "用户登录")
    public Result<LoginVO> login(@RequestBody LoginDTO loginDTO) {
        LoginVO loginVO = userService.login(loginDTO);
        return Result.success(loginVO, "登录成功");
    }

    @PostMapping("/logout")
    @Log(module = "认证", action = "退出", description = "用户退出")
    public Result<Void> logout(@RequestHeader("Authorization") String token) {
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        userService.logout(token);
        return Result.success(null, "退出成功");
    }

    @PostMapping("/register")
    @Log(module = "认证", action = "注册", description = "用户注册")
    public Result<Void> register(@RequestBody UserAddDTO userAddDTO) {
        userService.addUser(userAddDTO);
        return Result.success(null, "注册成功");
    }
}
