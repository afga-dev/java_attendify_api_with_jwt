package com.attendify.attendify_api.event.service;

import org.springframework.data.domain.Pageable;

import com.attendify.attendify_api.event.dto.EventFilter;
import com.attendify.attendify_api.event.dto.EventRequestDTO;
import com.attendify.attendify_api.event.dto.EventResponseDTO;
import com.attendify.attendify_api.event.dto.EventSimpleDTO;
import com.attendify.attendify_api.shared.dto.PageResponseDTO;

public interface EventService {
    EventResponseDTO create(EventRequestDTO dto);

    EventResponseDTO update(Long id, EventRequestDTO dto);

    void delete(Long id);

    EventResponseDTO findById(Long id);

    PageResponseDTO<EventSimpleDTO> findAll(EventFilter EventFilter, Pageable pageable);

    PageResponseDTO<EventSimpleDTO> findByCategory(Long categoryId, Pageable pageable);
}
