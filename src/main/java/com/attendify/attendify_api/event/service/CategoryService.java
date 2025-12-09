package com.attendify.attendify_api.event.service;

import org.springframework.data.domain.Pageable;

import com.attendify.attendify_api.event.dto.CategoryRequestDTO;
import com.attendify.attendify_api.event.dto.CategoryResponseDTO;
import com.attendify.attendify_api.event.dto.CategorySimpleDTO;
import com.attendify.attendify_api.shared.dto.PageResponseDTO;

public interface CategoryService {
    CategoryResponseDTO create(CategoryRequestDTO dto);

    CategoryResponseDTO update(Long id, CategoryRequestDTO dto);

    void delete(Long id);

    void restore(Long id);

    CategoryResponseDTO findById(Long id);

    PageResponseDTO<CategorySimpleDTO> findAll(Pageable pageable);

    PageResponseDTO<CategorySimpleDTO> findAllDeleted(Pageable pageable);

    PageResponseDTO<CategorySimpleDTO> findAllWithDeleted(Pageable pageable);
}
