package com.attendify.attendify_api.event.dto;

import java.time.LocalDateTime;
import java.util.Set;

import com.attendify.attendify_api.event.model.EventLocation;
import com.attendify.attendify_api.event.model.EventStatus;
import com.attendify.attendify_api.shared.validation.Sanitize;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class EventRequestDTO {
    @NotBlank(message = "Title is required")
    @Sanitize
    private String title;

    @NotBlank(message = "Description is required")
    @Sanitize
    private String description;

    @NotNull(message = "Start date is required")
    private LocalDateTime startDate;

    @NotNull(message = "End date is required")
    private LocalDateTime endDate;

    @NotNull(message = "Location is required")
    private EventLocation location;

    @NotNull(message = "Capacity is required")
    private Integer capacity;

    @NotNull(message = "Status is required")
    private EventStatus status;

    @NotEmpty(message = "Event must have at least one category")
    private Set<Long> categoryIds;
}
