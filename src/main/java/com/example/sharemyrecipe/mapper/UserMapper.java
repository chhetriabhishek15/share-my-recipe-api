package com.example.sharemyrecipe.mapper;


import com.example.sharemyrecipe.core.enums.Role;
import com.example.sharemyrecipe.dto.SignupRequest;
import com.example.sharemyrecipe.entity.User;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class UserMapper {

    public User toEntity(SignupRequest dto, String passwordHash, Set<Role> roles) {
        return User.builder()
                .email(dto.getEmail().toLowerCase())
                .passwordHash(passwordHash)
                .handle(dto.getHandle())
                .displayName(dto.getDisplayName())
                .roles(roles)
                .build();
    }
}