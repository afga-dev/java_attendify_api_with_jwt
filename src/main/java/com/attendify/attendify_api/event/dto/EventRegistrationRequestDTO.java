package com.attendify.attendify_api.event.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record EventRegistrationRequestDTO(
        @NotNull(message = "Event ID is required") Long eventId) {
}
