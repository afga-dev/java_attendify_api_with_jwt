package com.attendify.attendify_api.event.mapper;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import com.attendify.attendify_api.event.dto.EventRegistrationResponseDTO;
import com.attendify.attendify_api.event.model.Event;
import com.attendify.attendify_api.event.model.EventRegistration;
import com.attendify.attendify_api.shared.dto.PageResponseDTO;
import com.attendify.attendify_api.user.model.User;

@Component
public class EventRegistrationMapper {
    public EventRegistration toEntity(User user, Event event) {
        return EventRegistration.builder()
                .user(user)
                .event(event)
                .checkedIn(false)
                .build();
    }

    public EventRegistrationResponseDTO toResponse(EventRegistration eventRegistration) {
        return EventRegistrationResponseDTO.builder()
                .id(eventRegistration.getId())
                .userId(eventRegistration.getUser().getId())
                .eventId(eventRegistration.getEvent().getId())
                .checkedIn(eventRegistration.getCheckedIn())
                .createdAt(eventRegistration.getCreatedAt())
                .build();
    }

    public PageResponseDTO<EventRegistrationResponseDTO> toPageResponse(Page<EventRegistration> page) {
        return PageResponseDTO.<EventRegistrationResponseDTO>builder()
                .items(page.getContent().stream()
                        .map(this::toResponse)
                        .toList())
                .page(page.getNumber())
                .size(page.getSize())
                .totalItems(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .isLast(page.isLast())
                .build();
    }
}
