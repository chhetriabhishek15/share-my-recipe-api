package com.example.sharemyrecipe.service;


import com.example.sharemyrecipe.dto.SignupRequest;
import com.example.sharemyrecipe.entity.User;
import org.springframework.security.core.userdetails.UserDetailsService;


import java.util.Optional;
import java.util.UUID;

public interface UserService extends UserDetailsService {
    User signup(SignupRequest request);
    Optional<User> findByEmail(String email);
    Optional<User> findById(UUID id);
}