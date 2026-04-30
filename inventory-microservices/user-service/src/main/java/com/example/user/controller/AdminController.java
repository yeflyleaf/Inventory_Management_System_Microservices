package com.example.user.controller;

import com.example.common.annotation.Log;
import com.example.common.domain.Result;
import com.example.user.dto.UserAddDTO;
import com.example.user.entity.User;
import com.example.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 管理员控制器 - 用户管理
 */
@RestController
@RequestMapping("/admin/users")
@CrossOrigin
public class AdminController {

    @Autowired
    private UserService userService;

    /**
     * 获取所有用户列表
     */
    @GetMapping
    public Result<List<User>> getAllUsers(@RequestParam(required = false) String name, 
                                          @RequestParam(required = false) String role) {
        List<User> users = userService.getUsers(name, role);
        // 隐藏密码
        users.forEach(u -> u.setPassword(null));
        return Result.success(users);
    }
    
    /**
     * 获取单个用户详情
     */
    @GetMapping("/{id}")
    public Result<User> getUserById(@PathVariable Long id) {
        User user = userService.getUserById(id);
        if (user != null) {
            user.setPassword(null);
        }
        return Result.success(user);
    }
    
    /**
     * 创建新用户
     */
    @PostMapping
    @Log(module = "用户管理", action = "新增用户", description = "创建新用户")
    public Result<Void> createUser(@RequestBody UserAddDTO userAddDTO) {
        userService.addUser(userAddDTO);
        return Result.success(null, "用户创建成功");
    }
    
    /**
     * 更新用户信息
     */
    @PutMapping("/{id}")
    @Log(module = "用户管理", action = "更新用户", description = "更新用户信息")
    public Result<Void> updateUser(@PathVariable Long id, @RequestBody UserAddDTO userAddDTO) {
        userAddDTO.setId(id);
        userService.updateUser(userAddDTO);
        return Result.success(null, "用户更新成功");
    }
    
    /**
     * 更新用户状态（启用/禁用）
     */
    @PutMapping("/{id}/status")
    @Log(module = "用户管理", action = "更新状态", description = "更新用户状态")
    public Result<Void> updateUserStatus(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        Integer status = body.get("status");
        userService.updateUserStatus(id, status);
        String statusText = (status != null && status == 1) ? "启用" : "禁用";
        return Result.success(null, "用户已" + statusText);
    }

    /**
     * 删除用户
     */
    @DeleteMapping("/{id}")
    @Log(module = "用户管理", action = "删除用户", description = "删除用户")
    public Result<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return Result.success(null, "用户已删除");
    }
}

