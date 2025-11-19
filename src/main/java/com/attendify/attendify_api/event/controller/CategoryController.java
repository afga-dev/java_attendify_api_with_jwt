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

import com.attendify.attendify_api.event.dto.CategoryRequestDTO;
import com.attendify.attendify_api.event.dto.CategoryResponseDTO;
import com.attendify.attendify_api.event.dto.CategorySimpleDTO;
import com.attendify.attendify_api.event.service.CategoryService;
import com.attendify.attendify_api.shared.dto.PageResponseDTO;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/attendify/v1/categories")
@RequiredArgsConstructor
public class CategoryController {
    private final CategoryService categoryService;

    @PostMapping
    public ResponseEntity<CategoryResponseDTO> createCategory(
            @Valid @RequestBody CategoryRequestDTO dto) {
        CategoryResponseDTO created = categoryService.create(dto);

        return ResponseEntity.created(URI.create("/attendify/v1/categories/" + created.getId())).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CategoryResponseDTO> updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody CategoryRequestDTO dto) {
        CategoryResponseDTO updated = categoryService.update(id, dto);

        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(
            @PathVariable Long id) {
        categoryService.delete(id);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponseDTO> getCategory(
            @PathVariable Long id) {
        return ResponseEntity.ok(categoryService.findById(id));
    }

    @GetMapping
    public ResponseEntity<PageResponseDTO<CategorySimpleDTO>> getAllCategories(Pageable pageable) {
        return ResponseEntity.ok(categoryService.findAll(pageable));
    }
}
