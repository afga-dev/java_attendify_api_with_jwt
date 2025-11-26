package com.attendify.attendify_api.event.controller;

import java.net.URI;

import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.attendify.attendify_api.event.dto.EventRegistrationRequestDTO;
import com.attendify.attendify_api.event.dto.EventRegistrationResponseDTO;
import com.attendify.attendify_api.event.service.EventRegistrationService;
import com.attendify.attendify_api.shared.annotation.eventregistration.CanCreateEventRegistration;
import com.attendify.attendify_api.shared.annotation.eventregistration.CanDeleteEventRegistration;
import com.attendify.attendify_api.shared.annotation.eventregistration.CanReadAdminEventRegistration;
import com.attendify.attendify_api.shared.annotation.eventregistration.CanReadDeletedEventRegistration;
import com.attendify.attendify_api.shared.annotation.eventregistration.CanReadEventRegistration;
import com.attendify.attendify_api.shared.annotation.eventregistration.CanRestoreEventRegistration;
import com.attendify.attendify_api.shared.annotation.eventregistration.CanUpdateEventRegistration;
import com.attendify.attendify_api.shared.dto.PageResponseDTO;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/attendify/v1/registrations")
@RequiredArgsConstructor
public class EventRegistrationController {
    private final EventRegistrationService eventRegistrationService;

    @PostMapping
    @CanCreateEventRegistration
    public ResponseEntity<EventRegistrationResponseDTO> createRegistration(
            @Valid @RequestBody EventRegistrationRequestDTO dto) {
        EventRegistrationResponseDTO created = eventRegistrationService.create(dto);

        return ResponseEntity.created(URI.create("/attendify/v1/registrations/" + created.id())).body(created);
    }

    @DeleteMapping("/{id}")
    @CanDeleteEventRegistration
    public ResponseEntity<Void> deleteRegistration(
            @PathVariable Long id) {
        eventRegistrationService.delete(id);

        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/restore")
    @CanRestoreEventRegistration
    public ResponseEntity<Void> restoreRegistration(
            @PathVariable Long id) {
        eventRegistrationService.restore(id);

        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/check-in")
    @CanUpdateEventRegistration
    public ResponseEntity<EventRegistrationResponseDTO> checkIn(
            @PathVariable Long id) {
        EventRegistrationResponseDTO checkIn = eventRegistrationService.checkIn(id);

        return ResponseEntity.ok(checkIn);
    }

    @GetMapping("/event/{eventId}")
    @CanReadAdminEventRegistration
    public ResponseEntity<PageResponseDTO<EventRegistrationResponseDTO>> getUsersByEvent(
            @PathVariable Long eventId,
            Pageable pageable) {
        return ResponseEntity.ok(eventRegistrationService.getUsersByEvent(eventId, pageable));
    }

    @GetMapping("/my")
    @CanReadEventRegistration
    public ResponseEntity<PageResponseDTO<EventRegistrationResponseDTO>> getMyEvents(Pageable pageable) {
        return ResponseEntity.ok(eventRegistrationService.getMyEvents(pageable));
    }

    @GetMapping("/deleted")
    @CanReadDeletedEventRegistration
    public ResponseEntity<PageResponseDTO<EventRegistrationResponseDTO>> getAllEventRegistrationsDeleted(
            Pageable pageable) {
        return ResponseEntity.ok(eventRegistrationService.findAllDeleted(pageable));
    }

    @GetMapping("/including-deleted")
    @CanReadDeletedEventRegistration
    public ResponseEntity<PageResponseDTO<EventRegistrationResponseDTO>> getAllEventRegistrationsIncludingDeleted(
            Pageable pageable) {
        return ResponseEntity.ok(eventRegistrationService.findAllIncludingDeleted(pageable));
    }
}
