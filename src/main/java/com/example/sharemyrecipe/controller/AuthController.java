package com.example.sharemyrecipe.controller;

import com.example.sharemyrecipe.dto.AuthResponse;
import com.example.sharemyrecipe.dto.LoginRequest;
import com.example.sharemyrecipe.dto.SignupRequest;
import com.example.sharemyrecipe.entity.User;
import com.example.sharemyrecipe.security.JwtUtil;
import com.example.sharemyrecipe.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
@Validated
public class AuthController {

    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/signup")
    @Transactional
    public ResponseEntity<?> signup(@Valid @RequestBody SignupRequest req) {
        log.debug("Signup request for email={}", req.getEmail());
        User created = userService.signup(req);
        return ResponseEntity.ok().body("signup successful; id=" + created.getId());
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest req) {
        log.debug("Login attempt for email={}", req.getEmail());
        var userOpt = userService.findByEmail(req.getEmail().toLowerCase());
        if (userOpt.isEmpty()) {
            log.warn("Login failed: user not found {}", req.getEmail());
            return ResponseEntity.status(401).build();
        }
        User user = userOpt.get();
        if (!passwordEncoder.matches(req.getPassword(), user.getPasswordHash())) {
            log.warn("Login failed: invalid credentials {}", req.getEmail());
            return ResponseEntity.status(401).build();
        }
        String token = jwtUtil.generateToken(user.getEmail());
        AuthResponse resp = new AuthResponse(token, "Bearer", jwtUtil.getExpiresInSeconds());
        log.info("User logged in: id={}, email={}", user.getId(), user.getEmail());
        return ResponseEntity.ok(resp);
    }
}