package com.attendify.attendify_api.auth.dto;

import com.attendify.attendify_api.shared.validation.Sanitize;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequestDTO(
        @NotBlank(message = "Email is required") @Email(message = "Invalid email format") @Sanitize String email,

        @NotBlank(message = "Password is required") @Size(min = 8, message = "Password must be at least 8 characters long") @Sanitize String password) {
}
