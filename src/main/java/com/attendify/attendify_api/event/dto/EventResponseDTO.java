package com.attendify.attendify_api.event.dto;

import java.time.LocalDateTime;
import java.util.Set;

import com.attendify.attendify_api.event.model.EventLocation;
import com.attendify.attendify_api.event.model.EventStatus;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EventResponseDTO {
    private Long id;
    private String title;
    private String description;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private EventLocation location;
    private Integer capacity;
    private EventStatus status;
    private Set<Long> registeredUserIds;
    private Set<Long> categoryIds;
}
