package com.attendify.attendify_api.event.dto;

import lombok.Builder;

@Builder
public record CategorySimpleDTO(
        Long id,
        String name) {
}
