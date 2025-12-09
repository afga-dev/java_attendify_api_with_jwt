package com.attendify.attendify_api.event.controller;

import java.net.URI;

import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
import com.attendify.attendify_api.shared.dto.PageResponseDTO;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/attendify/v1/registrations")
@RequiredArgsConstructor
public class EventRegistrationController {
    private final EventRegistrationService eventRegistrationService;

    @PostMapping
    @PreAuthorize("hasAuthority('EVENT_REGISTRATION_CREATE')")
    public ResponseEntity<EventRegistrationResponseDTO> createRegistration(
            @Valid @RequestBody EventRegistrationRequestDTO dto) {
        EventRegistrationResponseDTO created = eventRegistrationService.create(dto);

        // Returns 201 Created with URI pointing to new resource
        return ResponseEntity.created(URI.create("/attendify/v1/registrations/" + created.id())).body(created);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('EVENT_REGISTRATION_DELETE')")
    public ResponseEntity<Void> deleteRegistration(
            @PathVariable Long id) {
        eventRegistrationService.delete(id);

        // No content returned after successful deletion
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/restore")
    @PreAuthorize("hasAuthority('EVENT_REGISTRATION_RESTORE')")
    public ResponseEntity<Void> restoreRegistration(
            @PathVariable Long id) {
        eventRegistrationService.restore(id);

        // No content returned after successful restoration
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/check-in")
    @PreAuthorize("hasAuthority('EVENT_REGISTRATION_CHECKIN')")
    public ResponseEntity<EventRegistrationResponseDTO> checkIn(
            @PathVariable Long id) {
        EventRegistrationResponseDTO checkIn = eventRegistrationService.checkIn(id);

        return ResponseEntity.ok(checkIn);
    }

    @GetMapping("/{eventId}/registrations")
    @PreAuthorize("hasAuthority('EVENT_REGISTRATION_READ_BY_EVENT')")
    public ResponseEntity<PageResponseDTO<EventRegistrationResponseDTO>> getUsersByEvent(
            @PathVariable Long eventId,
            Pageable pageable) {
        return ResponseEntity.ok(eventRegistrationService.getUsersByEvent(eventId, pageable));
    }

    @GetMapping("/me")
    public ResponseEntity<PageResponseDTO<EventRegistrationResponseDTO>> getMyEvents(Pageable pageable) {
        return ResponseEntity.ok(eventRegistrationService.getMyEvents(pageable));
    }

    @GetMapping("/deleted")
    @PreAuthorize("hasAuthority('EVENT_REGISTRATION_READ_DELETED')")
    public ResponseEntity<PageResponseDTO<EventRegistrationResponseDTO>> getAllEventRegistrationsDeleted(
            Pageable pageable) {
        return ResponseEntity.ok(eventRegistrationService.findAllDeleted(pageable));
    }

    @GetMapping("/with-deleted")
    @PreAuthorize("hasAuthority('EVENT_REGISTRATION_READ_WITH_DELETED')")
    public ResponseEntity<PageResponseDTO<EventRegistrationResponseDTO>> getAllEventRegistrationsIncludingDeleted(
            Pageable pageable) {
        return ResponseEntity.ok(eventRegistrationService.findAllWithDeleted(pageable));
    }
}
