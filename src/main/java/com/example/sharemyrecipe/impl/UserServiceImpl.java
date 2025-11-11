package com.example.sharemyrecipe.impl;

import com.example.sharemyrecipe.core.enums.Role;
import com.example.sharemyrecipe.dto.SignupRequest;
import com.example.sharemyrecipe.entity.User;
import com.example.sharemyrecipe.exception.ConflictException;
import com.example.sharemyrecipe.mapper.UserMapper;
import com.example.sharemyrecipe.repository.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import com.example.sharemyrecipe.service.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public User signup(SignupRequest request) {
        log.debug("Attempting signup for email={}", request.getEmail());
        if (userRepository.existsByEmail(request.getEmail().toLowerCase())) {
            log.warn("Signup conflict: email already exists: {}", request.getEmail());
            throw new ConflictException("Email already registered");
        }
        // optional handle uniqueness check
        if (request.getHandle() != null && userRepository.findByHandle(request.getHandle()).isPresent()) {
            log.warn("Signup conflict: handle already exists: {}", request.getHandle());
            throw new ConflictException("Handle already taken");
        }

        // determine role — default to CHEF for this app, but map safe values
        Role role = switch (request.getRequestedRole() == null ? "CHEF" : request.getRequestedRole().toUpperCase()) {
            case "ADMIN" -> Role.ROLE_ADMIN;
            case "USER" -> Role.ROLE_USER;
            default -> Role.ROLE_CHEF;
        };

        Set<Role> roles = EnumSet.of(role);

        String encoded = passwordEncoder.encode(request.getPassword());
        User user = userMapper.toEntity(request, encoded, roles);
        User saved = userRepository.save(user);
        log.info("User signed up: id={}, email={}", saved.getId(), saved.getEmail());
        return saved;
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email.toLowerCase());
    }

    @Override
    public Optional<User> findById(UUID id) {
        return userRepository.findById(id);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        var user = userRepository.findByEmail(username.toLowerCase())
                .orElseThrow(() -> {
                    log.warn("User not found by username: {}", username);
                    return new UsernameNotFoundException("User not found");
                });

        var authorities = user.getRoles().stream()
                .map(r -> new SimpleGrantedAuthority(r.name()))
                .toList();

        return new org.springframework.security.core.userdetails.User(user.getEmail(), user.getPasswordHash(), authorities);
    }
}