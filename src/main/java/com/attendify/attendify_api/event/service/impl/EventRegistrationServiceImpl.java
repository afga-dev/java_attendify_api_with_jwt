package com.attendify.attendify_api.event.service.impl;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.attendify.attendify_api.event.dto.EventRegistrationRequestDTO;
import com.attendify.attendify_api.event.dto.EventRegistrationResponseDTO;
import com.attendify.attendify_api.event.entity.Event;
import com.attendify.attendify_api.event.entity.EventRegistration;
import com.attendify.attendify_api.event.mapper.EventRegistrationMapper;
import com.attendify.attendify_api.event.repository.EventRegistrationRepository;
import com.attendify.attendify_api.event.repository.EventRepository;
import com.attendify.attendify_api.event.service.EventRegistrationService;
import com.attendify.attendify_api.shared.dto.PageResponseDTO;
import com.attendify.attendify_api.shared.exception.BadRequestException;
import com.attendify.attendify_api.shared.exception.NotFoundException;
import com.attendify.attendify_api.shared.security.SecurityUtils;
import com.attendify.attendify_api.user.entity.User;
import com.attendify.attendify_api.user.entity.enums.Permission;
import com.attendify.attendify_api.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EventRegistrationServiceImpl implements EventRegistrationService {
    private final EventRegistrationMapper eventRegistrationMapper;
    private final EventRegistrationRepository eventRegistrationRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final SecurityUtils securityUtils;

    @Override
    @Transactional
    public EventRegistrationResponseDTO create(EventRegistrationRequestDTO dto) {
        Long userId = securityUtils.getAuthenticatedUserId();
        User user = getUserOrElseThrow(userId);
        Event event = getEventOrElseThrow(dto.eventId());

        if (eventRegistrationRepository.existsByUser_IdAndEvent_Id(user.getId(), event.getId()))
            throw new BadRequestException("User is already registered for this event");

        if (event.getEndDate().isBefore(LocalDateTime.now()))
            throw new BadRequestException("Cannot register for a past event");

        long currentRegistrations = eventRegistrationRepository.countByEvent_Id(event.getId());
        if (currentRegistrations >= event.getCapacity())
            throw new BadRequestException("Event is at full capacity");

        EventRegistration eventRegistration = eventRegistrationMapper.toEntity(user, event);

        eventRegistrationRepository.save(eventRegistration);

        return eventRegistrationMapper.toResponse(eventRegistration);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        EventRegistration registration = getEventRegistrationWithDeletedOrElseThrow(id);

        if (registration.getDeletedAt() != null)
            throw new BadRequestException("Event registration is deleted");

        // Verify the current user is the owner or has force delete permission
        securityUtils.checkOwnerOrPermission(registration.getUser().getId(), Permission.EVENT_REGISTRATION_FORCE_DELETE,
                "delete this registration");

        if (!securityUtils.hasPermission(Permission.EVENT_REGISTRATION_FORCE_DELETE)) {
            if (registration.getCheckedIn())
                throw new BadRequestException("Cannot cancel after check-in");

            if (registration.getEvent().getStartDate().isBefore(LocalDateTime.now()))
                throw new BadRequestException("Cannot cancel after the event has started");
        }

        registration.softDelete(securityUtils.getCurrentAuditorId());

        eventRegistrationRepository.save(registration);
    }

    @Override
    @Transactional
    public void restore(Long id) {
        EventRegistration eventRegistration = getEventRegistrationWithDeletedOrElseThrow(id);

        if (eventRegistration.getDeletedAt() == null)
            throw new BadRequestException("Event registration is not deleted");

        eventRegistration.restore();

        eventRegistrationRepository.save(eventRegistration);
    }

    @Override
    @Transactional
    public EventRegistrationResponseDTO checkIn(Long id) {
        EventRegistration registration = getEventRegistrationOrElseThrow(id);

        if (registration.getCheckedIn())
            throw new BadRequestException("User already checked in");

        // Verify the current user is the owner of the event or has force check in permission
        securityUtils.checkOwnerOrPermission(registration.getEvent().getCreatedBy(),
                Permission.EVENT_REGISTRATION_FORCE_CHECKIN, "check in this registration");

        if (!securityUtils.hasPermission(Permission.EVENT_REGISTRATION_FORCE_CHECKIN)
                && registration.getEvent().getStartDate().isAfter(LocalDateTime.now()))
            throw new BadRequestException("Cannot check in before event starts");

        registration.setCheckedIn(true);

        return eventRegistrationMapper.toResponse(registration);
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
        Long userId = securityUtils.getAuthenticatedUserId();

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
    public PageResponseDTO<EventRegistrationResponseDTO> findAllWithDeleted(
            Pageable pageable) {
        Page<EventRegistration> page = eventRegistrationRepository.findAllWithDeleted(pageable);

        return eventRegistrationMapper.toPageResponse(page);
    }

    // Helper that fetch an user
    private User getUserOrElseThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found"));
    }

    // Helper that fetch an event
    private Event getEventOrElseThrow(Long id) {
        return eventRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new NotFoundException("Event not found"));
    }

    // Helper that fetch a registration
    private EventRegistration getEventRegistrationOrElseThrow(Long id) {
        return eventRegistrationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Registration not found"));
    }

    // Helper that fetch a registration including soft-deleted
    private EventRegistration getEventRegistrationWithDeletedOrElseThrow(Long id) {
        return eventRegistrationRepository.findByIdWithDeleted(id)
                .orElseThrow(() -> new NotFoundException("Registrarion not found"));
    }
}
