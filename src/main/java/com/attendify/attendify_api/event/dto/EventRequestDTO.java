package com.attendify.attendify_api.event.dto;

import java.time.LocalDateTime;
import java.util.Set;

import com.attendify.attendify_api.event.model.EventLocation;
import com.attendify.attendify_api.event.model.EventStatus;
import com.attendify.attendify_api.shared.validation.Sanitize;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record EventRequestDTO(
        @NotBlank(message = "Title is required") @Sanitize String title,

        @NotBlank(message = "Description is required") @Sanitize String description,

        @NotNull(message = "Start date is required") LocalDateTime startDate,

        @NotNull(message = "End date is required") LocalDateTime endDate,

        @NotNull(message = "Location is required") EventLocation location,

        @NotNull(message = "Capacity is required") Integer capacity,

        @NotNull(message = "Status is required") EventStatus status,

        @NotEmpty(message = "Event must have at least one category") Set<Long> categoryIds) {
}
