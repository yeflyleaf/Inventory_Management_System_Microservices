package com.example.user.mapper;

import com.example.user.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface UserMapper {
    int insert(User user);
    int update(User user);
    int updatePassword(@Param("id") Long id, @Param("password") String password);
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);
    int deleteById(Long id);
    User selectById(Long id);
    User selectByUsername(String username);
    List<User> selectAll();
    List<User> selectByCondition(@Param("name") String name, @Param("role") String role);
}
