package com.attendify.attendify_api.event.dto;

import com.attendify.attendify_api.shared.validation.Sanitize;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record CategoryRequestDTO(
        @NotBlank(message = "Name is required") @Sanitize String name,

        @NotBlank(message = "Description is required") @Sanitize String description) {
}
