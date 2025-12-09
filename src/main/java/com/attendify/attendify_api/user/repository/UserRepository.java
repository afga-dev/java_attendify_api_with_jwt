package com.attendify.attendify_api.user.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.attendify.attendify_api.user.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    // Fetch an user by ID, including soft-deleted entries
    @Query(value = "SELECT * FROM users WHERE user_id = :id", nativeQuery = true)
    Optional<User> findByIdWithDeleted(@Param("id") Long id);

    // Fetch all users that have been soft-deleted
    @Query(value = "SELECT * FROM users WHERE deleted_at IS NOT NULL", nativeQuery = true)
    Page<User> findAllDeleted(Pageable pageable);

    // Fetch all users, including both active and soft-deleted
    @Query(value = "SELECT * FROM users", nativeQuery = true)
    Page<User> findAllWithDeleted(Pageable pageable);
}
