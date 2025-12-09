package com.attendify.attendify_api.event.mapper;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import com.attendify.attendify_api.event.dto.CategorySimpleDTO;
import com.attendify.attendify_api.event.dto.EventRequestDTO;
import com.attendify.attendify_api.event.dto.EventResponseDTO;
import com.attendify.attendify_api.event.dto.EventSimpleDTO;
import com.attendify.attendify_api.event.entity.Category;
import com.attendify.attendify_api.event.entity.Event;
import com.attendify.attendify_api.shared.dto.PageResponseDTO;
import com.attendify.attendify_api.shared.mapper.PageMappingUtils;
import com.attendify.attendify_api.user.dto.UserSummaryDTO;
import com.attendify.attendify_api.user.mapper.UserMapper;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class EventMapper {
    private final UserMapper userMapper;
    private final CategoryMapper categoryMapper;

    // Converts DTO to new Event entity
    public Event toEntity(
            EventRequestDTO dto,
            Set<Category> categories) {
        return Event.builder()
                .title(dto.title())
                .description(dto.description())
                .startDate(dto.startDate())
                .endDate(dto.endDate())
                .location(dto.location())
                .capacity(dto.capacity())
                .status(dto.status())
                .categories(categories)
                .build();
    }

    // Updates existing Event entity with values from DTO
    public void updateEntity(
            Event event,
            EventRequestDTO dto,
            Set<Category> categories) {
        event.setTitle(dto.title());
        event.setDescription(dto.description());
        event.setStartDate(dto.startDate());
        event.setEndDate(dto.endDate());
        event.setLocation(dto.location());
        event.setCapacity(dto.capacity());
        event.setStatus(dto.status());

        event.getCategories().clear();
        event.getCategories().addAll(categories);
    }

    // Converts entity to detailed response DTO
    public EventResponseDTO toResponse(Event event) {
        Set<UserSummaryDTO> registeredUsers = Optional.ofNullable(event.getRegistrations())
                .orElse(Collections.emptySet())
                .stream()
                .map(reg -> userMapper.toSummary(reg.getUser()))
                .collect(Collectors.toSet());

        Set<CategorySimpleDTO> categories = Optional.ofNullable(event.getCategories())
                .orElse(Collections.emptySet())
                .stream()
                .map(categoryMapper::toSimple)
                .collect(Collectors.toSet());

        return EventResponseDTO.builder()
                .id(event.getId())
                .title(event.getTitle())
                .description(event.getDescription())
                .startDate(event.getStartDate())
                .endDate(event.getEndDate())
                .location(event.getLocation())
                .capacity(event.getCapacity())
                .status(event.getStatus())
                .registeredUsers(registeredUsers)
                .categories(categories)
                .build();
    }

    // Converts entity to simplified DTO
    public EventSimpleDTO toSimple(Event event) {
        return EventSimpleDTO.builder()
                .id(event.getId())
                .title(event.getTitle())
                .startDate(event.getStartDate())
                .endDate(event.getEndDate())
                .status(event.getStatus())
                .build();
    }

    // Converts a Spring Page of entities to a standardized paginated response
    public PageResponseDTO<EventSimpleDTO> toPageResponse(Page<Event> page) {
        return PageMappingUtils.toPageResponse(page, this::toSimple);
    }
}
