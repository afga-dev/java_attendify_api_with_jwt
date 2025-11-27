package com.attendify.attendify_api.auth.dto;

import com.attendify.attendify_api.shared.validation.Sanitize;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChangePasswordRequestDTO(
        @NotBlank(message = "Current password is required") @Size(min = 8, message = "Password must be at least 8 characters long") @Sanitize String currentPassword,

        @NotBlank(message = "New password is required") @Size(min = 8, message = "Password must be at least 8 characters long") @Sanitize String newPassword) {
}
