package com.attendify.attendify_api.event.repository;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.attendify.attendify_api.event.entity.Category;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    boolean existsByName(String name);

    // Fetch a category by ID, including soft-deleted entries
    @Query(value = "SELECT * FROM categories WHERE category_id = :id", nativeQuery = true)
    Optional<Category> findByIdWithDeleted(@Param("id") Long id);

    // Fetch all categories that have been soft-deleted
    @Query(value = "SELECT * FROM categories WHERE deleted_at IS NOT NULL", nativeQuery = true)
    Page<Category> findAllDeleted(Pageable pageable);

    // Fetch all categories, including both active and soft-deleted
    @Query(value = "SELECT * FROM categories", nativeQuery = true)
    Page<Category> findAllWithDeleted(Pageable pageable);
}
