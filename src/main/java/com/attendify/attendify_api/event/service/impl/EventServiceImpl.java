package com.attendify.attendify_api.event.service.impl;

import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.domain.AuditorAware;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.attendify.attendify_api.event.dto.EventFilter;
import com.attendify.attendify_api.event.dto.EventRequestDTO;
import com.attendify.attendify_api.event.dto.EventResponseDTO;
import com.attendify.attendify_api.event.dto.EventSimpleDTO;
import com.attendify.attendify_api.event.mapper.EventMapper;
import com.attendify.attendify_api.event.model.Category;
import com.attendify.attendify_api.event.model.Event;
import com.attendify.attendify_api.event.persistence.EventSpecification;
import com.attendify.attendify_api.event.repository.CategoryRepository;
import com.attendify.attendify_api.event.repository.EventRepository;
import com.attendify.attendify_api.event.service.EventService;
import com.attendify.attendify_api.shared.dto.PageResponseDTO;
import com.attendify.attendify_api.shared.exception.NotFoundException;

import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {
    private final EventMapper eventMapper;
    private final EventRepository eventRepository;
    private final CategoryRepository categoryRepository;
    private final EventSpecification eventSpecification;
    private final AuditorAware<Long> auditorAware;

    @Override
    @Transactional
    public EventResponseDTO create(EventRequestDTO dto) {
        Set<Category> categories = getCategoriesFromIds(dto.categoryIds());

        if (dto.endDate().isBefore(dto.startDate())) {
            throw new ValidationException("End date must be after start date");
        }

        Event entity = eventMapper.toEntity(dto, categories);

        Event event = eventRepository.save(entity);

        return eventMapper.toResponse(event);
    }

    @Override
    @Transactional
    public EventResponseDTO update(
            Long id,
            EventRequestDTO dto) {
        Event event = getEventOrElseThrow(id);
        Set<Category> categories = getCategoriesFromIds(dto.categoryIds());

        eventMapper.updateEntity(event, dto, categories);

        return eventMapper.toResponse(event);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Event event = getEventIncludingDeletedOrElseThrow(id);

        if (event.getDeleteAt() != null)
            throw new IllegalStateException("Event is deleted");

        Long userId = auditorAware.getCurrentAuditor().orElse(null);

        event.softDelete(userId);

        eventRepository.save(event);
    }

    @Override
    @Transactional
    public void restore(Long id) {
        Event event = getEventIncludingDeletedOrElseThrow(id);

        if (event.getDeleteAt() == null)
            throw new IllegalStateException("Event is not deleted");

        event.restore();

        eventRepository.save(event);
    }

    @Override
    @Transactional(readOnly = true)
    public EventResponseDTO findById(Long id) {
        Event event = getEventOrElseThrow(id);

        return eventMapper.toResponse(event);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponseDTO<EventSimpleDTO> findAll(
            EventFilter eventFilter,
            Pageable pageable) {
        Page<Event> page = eventRepository.findAll(eventSpecification.build(eventFilter), pageable);

        return eventMapper.toPageResponse(page);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponseDTO<EventSimpleDTO> findByCategory(
            Long categoryId,
            Pageable pageable) {
        Page<Event> page = eventRepository.findByCategories_Id(categoryId, pageable);

        return eventMapper.toPageResponse(page);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponseDTO<EventSimpleDTO> findAllDeleted(
            Pageable pageable) {
        Page<Event> page = eventRepository.findAllDeleted(pageable);

        return eventMapper.toPageResponse(page);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponseDTO<EventSimpleDTO> findAllIncludingDeleted(
            Pageable pageable) {
        Page<Event> page = eventRepository.findAllIncludingDeleted(pageable);

        return eventMapper.toPageResponse(page);
    }

    private Event getEventOrElseThrow(Long id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Event with id '" + id + "' not found"));
    }

    private Event getEventIncludingDeletedOrElseThrow(Long id) {
        return eventRepository.findByIdIncludingDeleted(id)
                .orElseThrow(() -> new NotFoundException("Event with id '" + id + "' not found"));
    }

    private Set<Category> getCategoriesFromIds(Set<Long> ids) {
        if (ids == null || ids.isEmpty())
            return Set.of();

        return ids.stream()
                .map(id -> categoryRepository.findById(id)
                        .orElseThrow(() -> new NotFoundException("Category with id '" + id + "' not found")))
                .collect(Collectors.toSet());
    }
}
