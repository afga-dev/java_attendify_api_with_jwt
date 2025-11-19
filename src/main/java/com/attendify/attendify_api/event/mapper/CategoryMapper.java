package com.attendify.attendify_api.event.mapper;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import com.attendify.attendify_api.event.dto.CategoryRequestDTO;
import com.attendify.attendify_api.event.dto.CategoryResponseDTO;
import com.attendify.attendify_api.event.dto.CategorySimpleDTO;
import com.attendify.attendify_api.event.model.Category;
import com.attendify.attendify_api.shared.dto.PageResponseDTO;

@Component
public class CategoryMapper {
    public Category toEntity(CategoryRequestDTO dto) {
        return Category.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .build();
    }

    public void updateEntity(Category category, CategoryRequestDTO dto) {
        category.setName(dto.getName());
        category.setDescription(dto.getDescription());
    }

    public CategoryResponseDTO toResponse(Category category) {
        return CategoryResponseDTO.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .build();
    }

    public CategorySimpleDTO toSimple(Category category) {
        return CategorySimpleDTO.builder()
                .id(category.getId())
                .name(category.getName())
                .build();
    }

    public PageResponseDTO<CategorySimpleDTO> toPageResponse(Page<Category> page) {
        return PageResponseDTO.<CategorySimpleDTO>builder()
                .items(page.getContent().stream()
                        .map(this::toSimple)
                        .toList())
                .page(page.getNumber())
                .size(page.getSize())
                .totalItems(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .isLast(page.isLast())
                .build();
    }
}
