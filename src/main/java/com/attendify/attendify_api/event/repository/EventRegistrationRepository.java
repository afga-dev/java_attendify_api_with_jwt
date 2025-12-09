package com.attendify.attendify_api.event.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.attendify.attendify_api.event.entity.EventRegistration;

@Repository
public interface EventRegistrationRepository extends JpaRepository<EventRegistration, Long> {
    Boolean existsByUser_IdAndEvent_Id(Long userId, Long eventId);

    long countByEvent_Id(Long eventId);

    // Fetch registrations for a specific event, eagerly fetching user and event to avoid N+1
    @Query("""
                SELECT er FROM EventRegistration er
                JOIN FETCH er.user
                JOIN FETCH er.event
                WHERE er.event.id = :eventId
            """)
    Page<EventRegistration> findByEvent_IdFetch(@Param("eventId") Long id, Pageable pageable);

    // Fetch registrations for a specific user, eagerly fetching user and event to avoid N+1
    @Query("""
                SELECT er FROM EventRegistration er
                JOIN FETCH er.user
                JOIN FETCH er.event
                WHERE er.user.id = :userId
            """)
    Page<EventRegistration> findByUser_IdFetch(@Param("userId") Long userId, Pageable pageable);

    // Fetch a registration by ID, including soft-deleted entries
    @Query(value = "SELECT * FROM event_registrations WHERE event_registration_id = :id", nativeQuery = true)
    Optional<EventRegistration> findByIdWithDeleted(@Param("id") Long id);

    // Fetch all registrations that have been soft-deleted
    @Query(value = "SELECT * FROM event_registrations WHERE deleted_at IS NOT NULL", nativeQuery = true)
    Page<EventRegistration> findAllDeleted(Pageable pageable);

    // Fetch all registrations, including both active and soft-deleted
    @Query(value = "SELECT * FROM event_registrations", nativeQuery = true)
    Page<EventRegistration> findAllWithDeleted(Pageable pageable);
}
