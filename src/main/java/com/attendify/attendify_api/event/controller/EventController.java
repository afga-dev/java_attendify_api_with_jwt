package com.attendify.attendify_api.event.controller;

import java.net.URI;

import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.attendify.attendify_api.event.dto.EventFilterDTO;
import com.attendify.attendify_api.event.dto.EventRequestDTO;
import com.attendify.attendify_api.event.dto.EventResponseDTO;
import com.attendify.attendify_api.event.dto.EventSimpleDTO;
import com.attendify.attendify_api.event.service.EventService;
import com.attendify.attendify_api.shared.dto.PageResponseDTO;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/attendify/v1/events")
@RequiredArgsConstructor
public class EventController {
    private final EventService eventService;

    @PostMapping
    @PreAuthorize("hasAuthority('EVENT_CREATE')")
    public ResponseEntity<EventResponseDTO> createEvent(
            @Valid @RequestBody EventRequestDTO dto) {
        EventResponseDTO created = eventService.create(dto);

        // Returns 201 Created with URI pointing to new resource
        return ResponseEntity.created(URI.create("/attendify/v1/events/" + created.id())).body(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('EVENT_UPDATE')")
    public ResponseEntity<EventResponseDTO> updateEvent(
            @PathVariable Long id,
            @Valid @RequestBody EventRequestDTO dto) {
        EventResponseDTO updated = eventService.update(id, dto);

        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('EVENT_DELETE')")
    public ResponseEntity<Void> deleteEvent(
            @PathVariable Long id) {
        eventService.delete(id);

        // No content returned after successful deletion
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/restore")
    @PreAuthorize("hasAuthority('EVENT_RESTORE')")
    public ResponseEntity<Void> restoreEvent(
            @PathVariable Long id) {
        eventService.restore(id);

        // No content returned after successful restoration
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<EventResponseDTO> getEvent(
            @PathVariable Long id) {
        return ResponseEntity.ok(eventService.findById(id));
    }

    @GetMapping
    public ResponseEntity<PageResponseDTO<EventSimpleDTO>> getAllEvents(
            @ModelAttribute EventFilterDTO eventFilter,
            Pageable pageable) {
        return ResponseEntity.ok(eventService.findAll(eventFilter, pageable));
    }

    @GetMapping("/{id}/category")
    public ResponseEntity<PageResponseDTO<EventSimpleDTO>> getByCategory(
            @PathVariable Long id,
            Pageable pageable) {
        return ResponseEntity.ok(eventService.findByCategory(id, pageable));
    }

    @GetMapping("/deleted")
    @PreAuthorize("hasAuthority('EVENT_READ_DELETED')")
    public ResponseEntity<PageResponseDTO<EventSimpleDTO>> getAllEventsDeleted(
            Pageable pageable) {
        return ResponseEntity.ok(eventService.findAllDeleted(pageable));
    }

    @GetMapping("/with-deleted")
    @PreAuthorize("hasAuthority('EVENT_READ_WITH_DELETED')")
    public ResponseEntity<PageResponseDTO<EventSimpleDTO>> getAllWithDeleted(
            Pageable pageable) {
        return ResponseEntity.ok(eventService.findAllWithDeleted(pageable));
    }
}
