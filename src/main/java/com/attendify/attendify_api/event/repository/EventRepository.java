package com.attendify.attendify_api.event.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.attendify.attendify_api.event.entity.Event;

import jakarta.persistence.LockModeType;

@Repository
public interface EventRepository extends JpaRepository<Event, Long>, JpaSpecificationExecutor<Event> {
    Page<Event> findByCategories_Id(Long categoryId, Pageable pageable);

    // Lock the event row for update to prevent concurrent modifications
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT e FROM Event e WHERE e.id = :id")
    Optional<Event> findByIdForUpdate(@Param("id") Long id);

    // Fetch an event by ID, including soft-deleted entries
    @Query(value = "SELECT * FROM events WHERE event_id = :id", nativeQuery = true)
    Optional<Event> findByIdWithDeleted(@Param("id") Long id);

    // Fetch all events that have been soft-deleted
    @Query(value = "SELECT * FROM events WHERE deleted_at IS NOT NULL", nativeQuery = true)
    Page<Event> findAllDeleted(Pageable pageable);

    // Fetch all events, including both active and soft-deleted
    @Query(value = "SELECT * FROM events", nativeQuery = true)
    Page<Event> findAllWithDeleted(Pageable pageable);
}
