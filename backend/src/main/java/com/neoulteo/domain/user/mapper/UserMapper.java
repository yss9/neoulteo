package com.neoulteo.domain.user.mapper;

import com.neoulteo.domain.user.dto.UserDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserMapper {
    boolean registerUser(UserDto user);

    UserDto login(@Param("email") String email, @Param("password") String password);

    UserDto findByEmail(@Param("email") String email);

    boolean updateUser(@Param("email") String email, @Param("name") String name,
            @Param("password") String password);

    boolean deleteUser(@Param("email") String email);
}
