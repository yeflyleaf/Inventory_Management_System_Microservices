package com.example.user.service;

import com.example.user.dto.LoginDTO;
import com.example.user.dto.UserAddDTO;
import com.example.user.vo.LoginVO;
import com.example.user.entity.User;
import java.util.List;

public interface UserService {
    LoginVO login(LoginDTO loginDTO);
    void logout(String token);
    void addUser(UserAddDTO userAddDTO);
    void updateUser(UserAddDTO userAddDTO);
    void updateUserStatus(Long id, Integer status);
    User getUserById(Long id);
    List<User> getAllUsers();
    List<User> getUsers(String name, String role);
    void deleteUser(Long id);
}
