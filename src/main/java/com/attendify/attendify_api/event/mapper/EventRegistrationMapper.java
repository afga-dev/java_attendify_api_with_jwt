package com.attendify.attendify_api.event.mapper;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import com.attendify.attendify_api.event.dto.EventRegistrationResponseDTO;
import com.attendify.attendify_api.event.entity.Event;
import com.attendify.attendify_api.event.entity.EventRegistration;
import com.attendify.attendify_api.shared.dto.PageResponseDTO;
import com.attendify.attendify_api.shared.mapper.PageMappingUtils;
import com.attendify.attendify_api.user.entity.User;

@Component
public class EventRegistrationMapper {
    // Converts DTO to new EventRegistration entity
    public EventRegistration toEntity(User user, Event event) {
        return EventRegistration.builder()
                .user(user)
                .event(event)
                .checkedIn(false)
                .build();
    }

    // Converts entity to detailed response DTO
    public EventRegistrationResponseDTO toResponse(EventRegistration eventRegistration) {
        return EventRegistrationResponseDTO.builder()
                .id(eventRegistration.getId())
                .userId(eventRegistration.getUser().getId())
                .eventId(eventRegistration.getEvent().getId())
                .checkedIn(eventRegistration.getCheckedIn())
                .createdAt(eventRegistration.getCreatedAt())
                .build();
    }

    // Converts a Spring Page of entities to a standardized paginated response
    public PageResponseDTO<EventRegistrationResponseDTO> toPageResponse(Page<EventRegistration> page) {
        return PageMappingUtils.toPageResponse(page, this::toResponse);
    }
}
