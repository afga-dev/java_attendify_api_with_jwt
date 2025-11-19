package com.attendify.attendify_api.event.dto;

import com.attendify.attendify_api.event.model.EventLocation;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventFilter {
    private String text;
    private Boolean onlyUpcoming;
    private EventLocation location;
}
