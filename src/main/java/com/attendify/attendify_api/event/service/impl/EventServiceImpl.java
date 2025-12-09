package com.attendify.attendify_api.event.service.impl;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.attendify.attendify_api.event.dto.EventFilterDTO;
import com.attendify.attendify_api.event.dto.EventRequestDTO;
import com.attendify.attendify_api.event.dto.EventResponseDTO;
import com.attendify.attendify_api.event.dto.EventSimpleDTO;
import com.attendify.attendify_api.event.entity.Category;
import com.attendify.attendify_api.event.entity.Event;
import com.attendify.attendify_api.event.mapper.EventMapper;
import com.attendify.attendify_api.event.persistence.EventSpecifications;
import com.attendify.attendify_api.event.repository.CategoryRepository;
import com.attendify.attendify_api.event.repository.EventRepository;
import com.attendify.attendify_api.event.service.EventService;
import com.attendify.attendify_api.shared.dto.PageResponseDTO;
import com.attendify.attendify_api.shared.exception.BadRequestException;
import com.attendify.attendify_api.shared.exception.NotFoundException;
import com.attendify.attendify_api.shared.security.SecurityUtils;
import com.attendify.attendify_api.user.entity.enums.Permission;

import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {
    private final EventMapper eventMapper;
    private final EventRepository eventRepository;
    private final CategoryRepository categoryRepository;
    private final EventSpecifications eventSpecification;
    private final SecurityUtils securityUtils;

    @Override
    @Transactional
    public EventResponseDTO create(EventRequestDTO dto) {
        Set<Category> categories = getCategoriesFromIds(dto.categoryIds());

        if (dto.endDate().isBefore(dto.startDate()))
            throw new ValidationException("End date must be after start date");

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

        // Verify the current user is the owner or has force update permission
        if (dto.endDate().isBefore(dto.startDate()))
            throw new ValidationException("End date must be after start date");

        securityUtils.checkOwnerOrPermission(event.getCreatedBy(), Permission.EVENT_FORCE_UPDATE, "update this event");

        eventMapper.updateEntity(event, dto, categories);

        return eventMapper.toResponse(event);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Event event = getEventWithDeletedOrElseThrow(id);

        if (event.getDeletedAt() != null)
            throw new BadRequestException("Event is deleted");

        // Verify the current user is the owner or has force delete permission
        securityUtils.checkOwnerOrPermission(event.getCreatedBy(), Permission.EVENT_FORCE_DELETE, "delete this event");

        event.softDelete(securityUtils.getCurrentAuditorId());

        eventRepository.save(event);
    }

    @Override
    @Transactional
    public void restore(Long id) {
        Event event = getEventWithDeletedOrElseThrow(id);

        if (event.getDeletedAt() == null)
            throw new BadRequestException("Event is not deleted");

        if (event.getEndDate().isBefore(LocalDateTime.now()))
            throw new BadRequestException("Cannot restore a past event");

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
            EventFilterDTO eventFilter,
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
    public PageResponseDTO<EventSimpleDTO> findAllWithDeleted(
            Pageable pageable) {
        Page<Event> page = eventRepository.findAllWithDeleted(pageable);

        return eventMapper.toPageResponse(page);
    }

    // Helper that fetch an event
    private Event getEventOrElseThrow(Long id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Event not found"));
    }

    // Helper that fetch an event including soft-deleted
    private Event getEventWithDeletedOrElseThrow(Long id) {
        return eventRepository.findByIdWithDeleted(id)
                .orElseThrow(() -> new NotFoundException("Event not found"));
    }

    // Helper that fetch the categories entities by their IDs
    private Set<Category> getCategoriesFromIds(Set<Long> ids) {
        if (ids == null || ids.isEmpty())
            return Set.of();

        Set<Category> categories = new HashSet<>(categoryRepository.findAllById(ids));

        if (categories.size() != ids.size())
            throw new NotFoundException("One or more categories were not found");

        return categories;
    }
}
