package com.attendify.attendify_api.event.dto;

import java.time.LocalDateTime;

import com.attendify.attendify_api.event.model.EventStatus;

import lombok.Data;

@Data
public class EventSimpleDTO {
    private Long id;
    private String title;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private EventStatus status;
}
