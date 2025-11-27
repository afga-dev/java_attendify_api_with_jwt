package com.attendify.attendify_api.auth.dto;

import com.attendify.attendify_api.shared.validation.Sanitize;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ChangeEmailRequestDTO(
        @NotBlank(message = "Email is required") @Email(message = "Invalid email format") @Sanitize String currentEmail,

        @NotBlank(message = "Email is required") @Email(message = "Invalid email format") @Sanitize String newEmail) {
}
