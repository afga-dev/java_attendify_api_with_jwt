package com.attendify.attendify_api.event.dto;

import java.time.LocalDateTime;
import java.util.Set;

import com.attendify.attendify_api.event.entity.enums.EventLocation;
import com.attendify.attendify_api.event.entity.enums.EventStatus;
import com.attendify.attendify_api.user.dto.UserSummaryDTO;

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
        Set<UserSummaryDTO> registeredUsers,
        Set<CategorySimpleDTO> categories) {
}
