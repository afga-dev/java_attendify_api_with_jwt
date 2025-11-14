package com.attendify.attendify_api.event.dto;

import java.time.LocalDateTime;
import java.util.Set;

import com.attendify.attendify_api.event.model.EventStatus;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class EventRequestDTO {
    @NotBlank
    private String title;

    @NotBlank
    private String description;

    @NotNull
    private LocalDateTime startDate;

    @NotNull
    private LocalDateTime endDate;

    @NotBlank
    private String location;

    @NotNull
    private Integer capacity;

    @NotNull
    private EventStatus status;

    private Set<Long> categoryId;
}
