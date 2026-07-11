package com.neoulteo.global.aspect;

import com.neoulteo.domain.user.dto.UserDto;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class UserPasswordAspect {
    private final PasswordEncoder pe;

    public UserPasswordAspect(PasswordEncoder pe) {
        this.pe = pe;
    }

    @Before("execution(* com.neoulteo.domain.user.dao.DbUserDao.registerUser(..)) && args(user)")
    public void encodeUserPassword(UserDto user) {
        user.setPassword(pe.encode(user.getPassword()));
    }
}
