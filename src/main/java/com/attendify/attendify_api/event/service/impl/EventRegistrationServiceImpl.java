package com.attendify.attendify_api.event.service.impl;

import java.time.LocalDateTime;

import org.springframework.data.domain.AuditorAware;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.attendify.attendify_api.event.dto.EventRegistrationRequestDTO;
import com.attendify.attendify_api.event.dto.EventRegistrationResponseDTO;
import com.attendify.attendify_api.event.mapper.EventRegistrationMapper;
import com.attendify.attendify_api.event.model.Event;
import com.attendify.attendify_api.event.model.EventRegistration;
import com.attendify.attendify_api.event.repository.EventRegistrationRepository;
import com.attendify.attendify_api.event.repository.EventRepository;
import com.attendify.attendify_api.event.service.EventRegistrationService;
import com.attendify.attendify_api.shared.dto.PageResponseDTO;
import com.attendify.attendify_api.shared.exception.BadRequestException;
import com.attendify.attendify_api.shared.exception.NotFoundException;
import com.attendify.attendify_api.user.model.User;
import com.attendify.attendify_api.user.repository.UserRepository;
import com.attendify.attendify_api.user.security.CustomUserDetails;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EventRegistrationServiceImpl implements EventRegistrationService {
    private final EventRegistrationMapper eventRegistrationMapper;
    private final EventRegistrationRepository eventRegistrationRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final AuditorAware<Long> auditorAware;

    @Override
    @Transactional
    public EventRegistrationResponseDTO create(EventRegistrationRequestDTO dto) {
        Long userId = getAuthenticatedUserId();
        User user = getUserOrElseThrow(userId);
        Event event = getEventOrElseThrow(dto.eventId());

        Boolean alreadyRegistred = eventRegistrationRepository.existsByUser_IdAndEvent_Id(user.getId(), event.getId());
        if (alreadyRegistred) {
            throw new BadRequestException("User is already registered for this event");
        }

        if (event.getEndDate().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Cannot register for a past event");
        }

        long currentRegistrations = eventRegistrationRepository.countByEvent_Id(event.getId());
        if (currentRegistrations >= event.getCapacity()) {
            throw new BadRequestException("Event is at full capacity");
        }

        EventRegistration eventRegistration = eventRegistrationMapper.toEntity(user, event);

        eventRegistrationRepository.save(eventRegistration);

        return eventRegistrationMapper.toResponse(eventRegistration);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        EventRegistration eventRegistration = getEventRegistrationIncludingDeletedOrElseThrow(id);

        if (eventRegistration.getDeleteAt() != null)
            throw new IllegalStateException("Event registration is deleted");

        Long userId = auditorAware.getCurrentAuditor().orElse(null);

        eventRegistration.softDelete(userId);

        eventRegistrationRepository.save(eventRegistration);
    }

    @Override
    @Transactional
    public void restore(Long id) {
        EventRegistration eventRegistration = getEventRegistrationIncludingDeletedOrElseThrow(id);

        if (eventRegistration.getDeleteAt() == null)
            throw new IllegalStateException("Event registration is not deleted");

        eventRegistration.restore();

        eventRegistrationRepository.save(eventRegistration);
    }

    @Override
    @Transactional
    public EventRegistrationResponseDTO checkIn(Long id) {
        EventRegistration eventRegistration = getEventRegistrationOrElseThrow(id);

        eventRegistration.setCheckedIn(true);

        return eventRegistrationMapper.toResponse(eventRegistration);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponseDTO<EventRegistrationResponseDTO> getUsersByEvent(
            Long id,
            Pageable pageable) {
        Page<EventRegistration> page = eventRegistrationRepository.findByEvent_IdFetch(id, pageable);

        return eventRegistrationMapper.toPageResponse(page);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponseDTO<EventRegistrationResponseDTO> getMyEvents(
            Pageable pageable) {
        Long userId = getAuthenticatedUserId();

        Page<EventRegistration> page = eventRegistrationRepository.findByUser_IdFetch(userId, pageable);

        return eventRegistrationMapper.toPageResponse(page);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponseDTO<EventRegistrationResponseDTO> findAllDeleted(
            Pageable pageable) {
        Page<EventRegistration> page = eventRegistrationRepository.findAllDeleted(pageable);

        return eventRegistrationMapper.toPageResponse(page);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponseDTO<EventRegistrationResponseDTO> findAllIncludingDeleted(
            Pageable pageable) {
        Page<EventRegistration> page = eventRegistrationRepository.findAllIncludingDeleted(pageable);

        return eventRegistrationMapper.toPageResponse(page);
    }

    private Long getAuthenticatedUserId() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        var principal = (CustomUserDetails) authentication.getPrincipal();
        return principal.getId();
    }

    private User getUserOrElseThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User with id '" + id + "' not found"));
    }

    private Event getEventOrElseThrow(Long id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Event with id '" + id + "' not found"));
    }

    private EventRegistration getEventRegistrationOrElseThrow(Long id) {
        return eventRegistrationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Registrarion with id '" + id + "' not found"));
    }

    private EventRegistration getEventRegistrationIncludingDeletedOrElseThrow(Long id) {
        return eventRegistrationRepository.findByIdIncludingDeleted(id)
                .orElseThrow(() -> new NotFoundException("Registrarion with id '" + id + "' not found"));
    }
}
