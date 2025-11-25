package com.attendify.attendify_api.event.dto;

import lombok.Builder;

@Builder
public record CategoryResponseDTO(
        Long id,
        String name,
        String description) {
}
