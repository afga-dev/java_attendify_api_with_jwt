package com.attendify.attendify_api.event.dto;

import com.attendify.attendify_api.event.model.enums.EventLocation;

public record EventFilter(
    String text,
    Boolean onlyUpcoming,
    EventLocation location
) {}
