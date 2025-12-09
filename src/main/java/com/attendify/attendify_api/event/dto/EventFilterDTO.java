package com.attendify.attendify_api.event.dto;

import com.attendify.attendify_api.event.entity.enums.EventLocation;

import lombok.Builder;

@Builder
public record EventFilterDTO(
        String text,
        Boolean onlyUpcoming,
        EventLocation location) {
}
