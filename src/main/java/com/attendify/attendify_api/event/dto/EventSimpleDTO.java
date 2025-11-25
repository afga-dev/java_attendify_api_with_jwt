package com.attendify.attendify_api.event.dto;

import java.time.LocalDateTime;

import com.attendify.attendify_api.event.model.EventStatus;

import lombok.Builder;

@Builder
public record EventSimpleDTO(
        Long id,
        String title,
        LocalDateTime startDate,
        LocalDateTime endDate,
        EventStatus status) {
}
