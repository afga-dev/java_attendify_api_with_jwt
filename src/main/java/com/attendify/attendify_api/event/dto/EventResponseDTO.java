package com.attendify.attendify_api.event.dto;

import java.time.LocalDateTime;
import java.util.Set;

import com.attendify.attendify_api.event.model.EventStatus;

import lombok.Data;

@Data
public class EventResponseDTO {
    private Long id;
    private String title;
    private String description;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String location;
    private Integer capacity;
    private EventStatus status;
    private Long CreatedBy;
    private Set<Long> registrationId;
    private Set<Long> categoryId;
}
