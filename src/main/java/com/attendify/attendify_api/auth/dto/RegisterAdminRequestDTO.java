package com.attendify.attendify_api.auth.dto;

import java.util.Set;

import com.attendify.attendify_api.shared.validation.Sanitize;
import com.attendify.attendify_api.user.entity.enums.Role;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record RegisterAdminRequestDTO(
        @NotBlank(message = "Email is required") @Email(message = "Invalid email format") @Sanitize String email,

        @NotBlank(message = "Password is required") @Size(min = 8, message = "Password must be at least 8 characters long") @Sanitize String password,

        @NotNull Set<Role> roles) {
}
