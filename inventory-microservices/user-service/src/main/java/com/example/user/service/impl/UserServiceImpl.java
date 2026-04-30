package com.example.user.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import com.example.user.mapper.UserMapper;
import com.example.user.dto.LoginDTO;
import com.example.user.dto.UserAddDTO;
import com.example.user.entity.User;
import com.example.user.service.UserService;
import com.example.common.utils.JwtUtils;
import com.example.user.vo.LoginVO;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private JwtUtils jwtUtils;

    @Override
    public LoginVO login(LoginDTO loginDTO) {
        User user = userMapper.selectByUsername(loginDTO.getUsername());
        if (user == null || !user.getPassword().equals(loginDTO.getPassword())) {
            throw new RuntimeException("Invalid username or password");
        }
        
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new RuntimeException("该账号已被禁用，请联系管理员");
        }
        user.setLastLoginAt(LocalDateTime.now());
        userMapper.update(user);

        LoginVO vo = new LoginVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());
        vo.setRole(user.getRole());
        vo.setAvatar(user.getAvatar());
        vo.setEmail(user.getEmail());
        vo.setPhone(user.getPhone());
        vo.setToken(jwtUtils.generateToken(user.getId(), user.getUsername(), user.getRole()));
        return vo;
    }

    @Override
    public void logout(String token) {
        jwtUtils.removeToken(token);
    }

    @Override
    @CacheEvict(value = "users", allEntries = true)
    public void addUser(UserAddDTO userAddDTO) {
        User user = new User();
        user.setUsername(userAddDTO.getUsername());
        user.setPassword(userAddDTO.getPassword());
        user.setNickname(userAddDTO.getNickname());
        user.setRole(userAddDTO.getRole());
        user.setPhone(userAddDTO.getPhone());
        user.setEmail(userAddDTO.getEmail());
        user.setStatus(1);
        user.setCreatedAt(LocalDateTime.now());
        userMapper.insert(user);
    }

    @Override
    @CacheEvict(value = "users", allEntries = true)
    public void updateUser(UserAddDTO userAddDTO) {
        User user = userMapper.selectById(userAddDTO.getId());
        if (user != null) {
            user.setNickname(userAddDTO.getNickname());
            user.setRole(userAddDTO.getRole());
            user.setPhone(userAddDTO.getPhone());
            user.setEmail(userAddDTO.getEmail());
            userMapper.update(user);
        }
    }

    @Override
    @CacheEvict(value = "users", allEntries = true)
    public void updateUserStatus(Long id, Integer status) {
        userMapper.updateStatus(id, status);
    }

    @Override
    @Cacheable(value = "users", key = "'user:' + #id")
    public User getUserById(Long id) {
        return userMapper.selectById(id);
    }

    @Override
    @Cacheable(value = "users", key = "'all'")
    public List<User> getAllUsers() {
        return userMapper.selectAll();
    }

    @Override
    public List<User> getUsers(String name, String role) {
        return userMapper.selectByCondition(name, role);
    }

    @Override
    @CacheEvict(value = "users", allEntries = true)
    public void deleteUser(Long id) {
        userMapper.deleteById(id);
    }
}
