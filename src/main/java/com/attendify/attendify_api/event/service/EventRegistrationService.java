package com.attendify.attendify_api.event.service;

import org.springframework.data.domain.Pageable;

import com.attendify.attendify_api.event.dto.EventRegistrationRequestDTO;
import com.attendify.attendify_api.event.dto.EventRegistrationResponseDTO;
import com.attendify.attendify_api.shared.dto.PageResponseDTO;

public interface EventRegistrationService {
    EventRegistrationResponseDTO create(EventRegistrationRequestDTO dto);

    void delete(Long id);

    EventRegistrationResponseDTO checkIn(Long id);

    PageResponseDTO<EventRegistrationResponseDTO> getUsersByEvent(Long id, Pageable pageable);

    PageResponseDTO<EventRegistrationResponseDTO> getMyEvents(Pageable pageable);
}
