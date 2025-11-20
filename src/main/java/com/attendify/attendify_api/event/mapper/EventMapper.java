package com.attendify.attendify_api.event.mapper;

import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import com.attendify.attendify_api.event.dto.EventRequestDTO;
import com.attendify.attendify_api.event.dto.EventResponseDTO;
import com.attendify.attendify_api.event.dto.EventSimpleDTO;
import com.attendify.attendify_api.event.model.Event;
import com.attendify.attendify_api.shared.dto.PageResponseDTO;
import com.attendify.attendify_api.event.model.Category;

@Component
public class EventMapper {
        public Event toEntity(
                        EventRequestDTO dto,
                        Set<Category> categories) {
                return Event.builder()
                                .title(dto.getTitle())
                                .description(dto.getDescription())
                                .startDate(dto.getStartDate())
                                .endDate(dto.getEndDate())
                                .location(dto.getLocation())
                                .capacity(dto.getCapacity())
                                .status(dto.getStatus())
                                .categories(categories)
                                .build();
        }

        public void updateEntity(
                        Event event,
                        EventRequestDTO dto,
                        Set<Category> categories) {
                event.setTitle(dto.getTitle());
                event.setDescription(dto.getDescription());
                event.setStartDate(dto.getStartDate());
                event.setEndDate(dto.getEndDate());
                event.setLocation(dto.getLocation());
                event.setCapacity(dto.getCapacity());
                event.setStatus(dto.getStatus());

                event.getCategories().clear();
                event.getCategories().addAll(categories);
        }

        public EventResponseDTO toResponse(Event event) {
                return EventResponseDTO.builder()
                                .id(event.getId())
                                .title(event.getTitle())
                                .description(event.getDescription())
                                .startDate(event.getStartDate())
                                .endDate(event.getEndDate())
                                .location(event.getLocation())
                                .capacity(event.getCapacity())
                                .status(event.getStatus())
                                .registeredUserIds(event.getRegistrations()
                                                .stream()
                                                .map(registration -> registration.getUser().getId())
                                                .collect(Collectors.toSet()))
                                .categoryIds(event.getCategories()
                                                .stream()
                                                .map(Category::getId)
                                                .collect(Collectors.toSet()))
                                .build();
        }

        public EventSimpleDTO toSimple(Event event) {
                return EventSimpleDTO.builder()
                                .id(event.getId())
                                .title(event.getTitle())
                                .startDate(event.getStartDate())
                                .endDate(event.getEndDate())
                                .status(event.getStatus())
                                .build();
        }

        public PageResponseDTO<EventSimpleDTO> toPageResponse(Page<Event> page) {
                return PageResponseDTO.<EventSimpleDTO>builder()
                                .items(page.getContent().stream()
                                                .map(this::toSimple)
                                                .toList())
                                .page(page.getNumber())
                                .size(page.getSize())
                                .totalItems(page.getTotalElements())
                                .totalPages(page.getTotalPages())
                                .isLast(page.isLast())
                                .build();
        }
}
