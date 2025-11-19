package com.attendify.attendify_api.event.persistence;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import com.attendify.attendify_api.event.dto.EventFilter;
import com.attendify.attendify_api.event.model.Event;

import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class EventSpecification {
    public Specification<Event> build(EventFilter eventFilter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (eventFilter.getText() != null && !eventFilter.getText().isBlank()) {
                String like = "%" + eventFilter.getText().toLowerCase() + "%";
                predicates.add(
                        cb.or(
                                cb.like(cb.lower(root.get("title")), like),
                                cb.like(cb.lower(root.get("description")), like)));
            }

            if (Boolean.TRUE.equals(eventFilter.getOnlyUpcoming())) {
                predicates.add(
                        cb.greaterThanOrEqualTo(root.get("startDate"), LocalDateTime.now()));
            }

            if (eventFilter.getLocation() != null) {
                predicates.add(
                        cb.equal(root.get("location"), eventFilter.getLocation()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
