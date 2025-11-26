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

import com.attendify.attendify_api.event.dto.EventFilter;
import com.attendify.attendify_api.event.dto.EventRequestDTO;
import com.attendify.attendify_api.event.dto.EventResponseDTO;
import com.attendify.attendify_api.event.dto.EventSimpleDTO;
import com.attendify.attendify_api.event.service.EventService;
import com.attendify.attendify_api.shared.annotation.event.CanCreateEvent;
import com.attendify.attendify_api.shared.annotation.event.CanDeleteEvent;
import com.attendify.attendify_api.shared.annotation.event.CanReadDeletedEvent;
import com.attendify.attendify_api.shared.annotation.event.CanReadEvent;
import com.attendify.attendify_api.shared.annotation.event.CanRestoreEvent;
import com.attendify.attendify_api.shared.annotation.event.CanUpdateEvent;
import com.attendify.attendify_api.shared.dto.PageResponseDTO;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/attendify/v1/events")
@RequiredArgsConstructor
public class EventController {
    private final EventService eventService;

    @PostMapping
    @CanCreateEvent
    public ResponseEntity<EventResponseDTO> createEvent(
            @Valid @RequestBody EventRequestDTO dto) {
        EventResponseDTO created = eventService.create(dto);

        return ResponseEntity.created(URI.create("/attendify/v1/events/" + created.id())).body(created);
    }

    @PutMapping("/{id}")
    @CanUpdateEvent
    public ResponseEntity<EventResponseDTO> updateEvent(
            @PathVariable Long id,
            @Valid @RequestBody EventRequestDTO dto) {
        EventResponseDTO updated = eventService.update(id, dto);

        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @CanDeleteEvent
    public ResponseEntity<Void> deleteEvent(
            @PathVariable Long id) {
        eventService.delete(id);

        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/restore")
    @CanRestoreEvent
    public ResponseEntity<Void> restoreEvent(
            @PathVariable Long id) {
        eventService.restore(id);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    @CanReadEvent
    public ResponseEntity<EventResponseDTO> getEvent(
            @PathVariable Long id) {
        return ResponseEntity.ok(eventService.findById(id));
    }

    @GetMapping
    @CanReadEvent
    public ResponseEntity<PageResponseDTO<EventSimpleDTO>> getAllEvents(
            EventFilter eventFilter,
            Pageable pageable) {
        return ResponseEntity.ok(eventService.findAll(eventFilter, pageable));
    }

    @GetMapping("/category/{id}")
    @CanReadEvent
    public ResponseEntity<PageResponseDTO<EventSimpleDTO>> getByCategory(
            @PathVariable Long id,
            Pageable pageable) {
        return ResponseEntity.ok(eventService.findByCategory(id, pageable));
    }

    @GetMapping("/deleted")
    @CanReadDeletedEvent
    public ResponseEntity<PageResponseDTO<EventSimpleDTO>> getAllEventsDeleted(
            Pageable pageable) {
        return ResponseEntity.ok(eventService.findAllDeleted(pageable));
    }

    @GetMapping("/including-deleted")
    @CanReadDeletedEvent
    public ResponseEntity<PageResponseDTO<EventSimpleDTO>> getByCategoryIncludingDeleted(
            Pageable pageable) {
        return ResponseEntity.ok(eventService.findAllIncludingDeleted(pageable));
    }
}
