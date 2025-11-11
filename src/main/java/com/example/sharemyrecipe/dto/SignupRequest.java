package com.example.sharemyrecipe.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SignupRequest {
    @Email
    @NotBlank
    private String email;

    @NotBlank
    @Size(min = 8, message = "password must be at least 8 characters")
    private String password;

    @Size(max = 100)
    private String handle;

    @Size(max = 200)
    private String displayName;

    private String requestedRole = "CHEF";
}