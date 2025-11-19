package com.attendify.attendify_api.event.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.attendify.attendify_api.event.model.EventRegistration;

@Repository
public interface EventRegistrationRepository extends JpaRepository<EventRegistration, Long> {
    Boolean existsByUser_IdAndEvent_Id(Long userId, Long eventId);

    long countByEvent_Id(Long eventId);

    @Query("""
                SELECT er FROM EventRegistration er
                JOIN FETCH er.user
                JOIN FETCH er.event
                WHERE er.event.id = :eventId
            """)
    Page<EventRegistration> findByEvent_IdFetch(@Param("eventId") Long id, Pageable pageable);

    @Query("""
                SELECT er FROM EventRegistration er
                JOIN FETCH er.user
                JOIN FETCH er.event
                WHERE er.user.id = :userId
            """)
    Page<EventRegistration> findByUser_IdFetch(@Param("userId") Long userId, Pageable pageable);
}
