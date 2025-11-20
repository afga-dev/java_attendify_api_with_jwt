package com.attendify.attendify_api.event.service.impl;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.attendify.attendify_api.event.dto.CategoryRequestDTO;
import com.attendify.attendify_api.event.dto.CategoryResponseDTO;
import com.attendify.attendify_api.event.dto.CategorySimpleDTO;
import com.attendify.attendify_api.event.mapper.CategoryMapper;
import com.attendify.attendify_api.event.model.Category;
import com.attendify.attendify_api.event.repository.CategoryRepository;
import com.attendify.attendify_api.event.service.CategoryService;
import com.attendify.attendify_api.shared.dto.PageResponseDTO;
import com.attendify.attendify_api.shared.exception.DuplicateException;
import com.attendify.attendify_api.shared.exception.NotFoundException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {
    private final CategoryMapper categoryMapper;
    private final CategoryRepository categoryRepository;
    private final AuditorAware<Long> auditorAware;

    @Override
    @Transactional
    public CategoryResponseDTO create(CategoryRequestDTO dto) {
        Category category = categoryMapper.toEntity(dto);

        if (categoryRepository.findByName(dto.getName()).isPresent()) {
            throw new DuplicateException("Category with name '" + dto.getName() + "' already exists");
        }

        categoryRepository.save(category);

        return categoryMapper.toResponse(category);
    }

    @Override
    @Transactional
    public CategoryResponseDTO update(Long id, CategoryRequestDTO dto) {
        Category category = getCategoryOrElseThrow(id);

        categoryMapper.updateEntity(category, dto);

        return categoryMapper.toResponse(category);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Category category = getCategoryOrElseThrow(id);

        Long userId = auditorAware.getCurrentAuditor().orElse(null);
        category.softDelete(userId);

        categoryRepository.save(category);
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryResponseDTO findById(Long id) {
        Category category = getCategoryOrElseThrow(id);

        return categoryMapper.toResponse(category);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponseDTO<CategorySimpleDTO> findAll(Pageable pageable) {
        Page<Category> page = categoryRepository.findAll(pageable);

        return categoryMapper.toPageResponse(page);
    }

    private Category getCategoryOrElseThrow(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Category with id '" + id + "' not found"));
    }
}
