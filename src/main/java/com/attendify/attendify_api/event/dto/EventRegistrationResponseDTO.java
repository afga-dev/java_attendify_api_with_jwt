package com.attendify.attendify_api.event.dto;

import java.time.LocalDateTime;

import lombok.Builder;

@Builder
public record EventRegistrationResponseDTO(
        Long id,
        Long userId,
        Long eventId,
        Boolean checkedIn,
        LocalDateTime createdAt) {
}
