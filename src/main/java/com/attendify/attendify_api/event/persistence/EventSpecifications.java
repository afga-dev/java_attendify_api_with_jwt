package com.attendify.attendify_api.event.persistence;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import com.attendify.attendify_api.event.dto.EventFilterDTO;
import com.attendify.attendify_api.event.entity.Event;

import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class EventSpecifications {

    // Builds a dynamic JPA Specification based on the provided filter DTO
    public Specification<Event> build(EventFilterDTO eventFilter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            var text = eventFilter.text();
            var location = eventFilter.location();

            // Filter by text in title or description
            if (text != null && !text.isBlank()) {
                String like = "%" + text.toLowerCase().trim() + "%";
                predicates.add(
                        cb.or(
                                cb.like(cb.lower(root.get("title")), like),
                                cb.like(cb.lower(root.get("description")), like)));
            }

            // Filter only upcoming events
            if (Boolean.TRUE.equals(eventFilter.onlyUpcoming())) {
                predicates.add(
                        cb.greaterThanOrEqualTo(
                                root.get("startDate"),
                                LocalDateTime.now(ZoneOffset.UTC)));
            }

            // Filter by exact location match
            if (location != null && !location.toString().isBlank()) {
                predicates.add(
                        cb.equal(root.get("location"), location));
            }

            // Combine all predicates with AND
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
