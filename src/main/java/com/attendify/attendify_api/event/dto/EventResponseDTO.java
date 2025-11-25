package com.attendify.attendify_api.event.dto;

import java.time.LocalDateTime;
import java.util.Set;

import com.attendify.attendify_api.event.model.EventLocation;
import com.attendify.attendify_api.event.model.EventStatus;

import lombok.Builder;

@Builder
public record EventResponseDTO(
        Long id,
        String title,
        String description,
        LocalDateTime startDate,
        LocalDateTime endDate,
        EventLocation location,
        Integer capacity,
        EventStatus status,
        Set<Long> registeredUserIds,
        Set<Long> categoryIds) {
}
