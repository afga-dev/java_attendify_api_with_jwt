package com.attendify.attendify_api.event.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class EventRegistrationRequestDTO {
    @NotNull
    private Long eventId;
}
