package com.attendify.attendify_api.event.mapper;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import com.attendify.attendify_api.event.dto.CategoryRequestDTO;
import com.attendify.attendify_api.event.dto.CategoryResponseDTO;
import com.attendify.attendify_api.event.dto.CategorySimpleDTO;
import com.attendify.attendify_api.event.entity.Category;
import com.attendify.attendify_api.shared.dto.PageResponseDTO;
import com.attendify.attendify_api.shared.mapper.PageMappingUtils;

@Component
public class CategoryMapper {
    // Converts DTO to new Category entity
    public Category toEntity(CategoryRequestDTO dto) {
        return Category.builder()
                .name(dto.name())
                .description(dto.description())
                .build();
    }

    // Updates existing Category entity with values from DTO
    public void updateEntity(Category category, CategoryRequestDTO dto) {
        category.setName(dto.name());
        category.setDescription(dto.description());
    }

    // Converts entity to detailed response DTO
    public CategoryResponseDTO toResponse(Category category) {
        return CategoryResponseDTO.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .build();
    }

    // Converts entity to simplified DTO
    public CategorySimpleDTO toSimple(Category category) {
        return CategorySimpleDTO.builder()
                .id(category.getId())
                .name(category.getName())
                .build();
    }

    // Converts a Spring Page of entities to a standardized paginated response
    public PageResponseDTO<CategorySimpleDTO> toPageResponse(Page<Category> page) {
        return PageMappingUtils.toPageResponse(page, this::toSimple);
    }
}
