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
import com.attendify.attendify_api.shared.dto.PageResponseDTO;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/attendify/v1/events")
@RequiredArgsConstructor
public class EventController {
    private final EventService eventService;

    @PostMapping
    public ResponseEntity<EventResponseDTO> createEvent(
            @Valid @RequestBody EventRequestDTO dto) {
        EventResponseDTO created = eventService.create(dto);

        return ResponseEntity.created(URI.create("/attendify/v1/events/" + created.getId())).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<EventResponseDTO> updateEvent(
            @PathVariable Long id,
            @Valid @RequestBody EventRequestDTO dto) {
        EventResponseDTO updated = eventService.update(id, dto);

        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEvent(
            @PathVariable Long id) {
        eventService.delete(id);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<EventResponseDTO> getEvent(
            @PathVariable Long id) {
        return ResponseEntity.ok(eventService.findById(id));
    }

    @GetMapping
    public ResponseEntity<PageResponseDTO<EventSimpleDTO>> getAllEvents(
            EventFilter eventFilter,
            Pageable pageable) {
        return ResponseEntity.ok(eventService.findAll(eventFilter, pageable));
    }

    @GetMapping("/category/{id}")
    public ResponseEntity<PageResponseDTO<EventSimpleDTO>> getByCategory(
            @PathVariable Long id,
            Pageable pageable) {
        return ResponseEntity.ok(eventService.findByCategory(id, pageable));
    }
}
