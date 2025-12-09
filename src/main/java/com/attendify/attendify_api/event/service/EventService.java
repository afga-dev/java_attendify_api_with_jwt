package com.attendify.attendify_api.event.service;

import org.springframework.data.domain.Pageable;

import com.attendify.attendify_api.event.dto.EventFilterDTO;
import com.attendify.attendify_api.event.dto.EventRequestDTO;
import com.attendify.attendify_api.event.dto.EventResponseDTO;
import com.attendify.attendify_api.event.dto.EventSimpleDTO;
import com.attendify.attendify_api.shared.dto.PageResponseDTO;

public interface EventService {
    EventResponseDTO create(EventRequestDTO dto);

    EventResponseDTO update(Long id, EventRequestDTO dto);

    void delete(Long id);

    void restore(Long id);

    EventResponseDTO findById(Long id);

    PageResponseDTO<EventSimpleDTO> findAll(EventFilterDTO EventFilter, Pageable pageable);

    PageResponseDTO<EventSimpleDTO> findByCategory(Long categoryId, Pageable pageable);

    PageResponseDTO<EventSimpleDTO> findAllDeleted(Pageable pageable);

    PageResponseDTO<EventSimpleDTO> findAllWithDeleted(Pageable pageable);
}
