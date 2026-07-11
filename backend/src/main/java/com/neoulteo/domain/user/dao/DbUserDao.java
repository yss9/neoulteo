package com.neoulteo.domain.user.dao;

import com.neoulteo.domain.user.mapper.UserMapper;
import com.neoulteo.domain.user.dto.UserDto;
import org.springframework.stereotype.Repository;

@Repository
public class DbUserDao {
    private final UserMapper userMapper;

    public DbUserDao(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    public boolean registerUser(UserDto user) {
        return userMapper.registerUser(user);
    }

    public UserDto login(String email, String password) {
        return userMapper.login(email, password);
    }

    public UserDto findByEmail(String email) {
        return userMapper.findByEmail(email);
    }

    public boolean updateUser(String email, String name, String password) {
        return userMapper.updateUser(email, name, password);
    }

    public boolean deleteUser(String email) {
        return userMapper.deleteUser(email);
    }
}
