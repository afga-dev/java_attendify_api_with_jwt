package com.attendify.attendify_api.event.service.impl;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.attendify.attendify_api.event.dto.CategoryRequestDTO;
import com.attendify.attendify_api.event.dto.CategoryResponseDTO;
import com.attendify.attendify_api.event.dto.CategorySimpleDTO;
import com.attendify.attendify_api.event.entity.Category;
import com.attendify.attendify_api.event.mapper.CategoryMapper;
import com.attendify.attendify_api.event.repository.CategoryRepository;
import com.attendify.attendify_api.event.service.CategoryService;
import com.attendify.attendify_api.shared.dto.PageResponseDTO;
import com.attendify.attendify_api.shared.exception.BadRequestException;
import com.attendify.attendify_api.shared.exception.DuplicateException;
import com.attendify.attendify_api.shared.exception.NotFoundException;
import com.attendify.attendify_api.shared.security.SecurityUtils;
import com.attendify.attendify_api.user.entity.enums.Permission;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {
    private final CategoryMapper categoryMapper;
    private final CategoryRepository categoryRepository;
    private final SecurityUtils securityUtils;

    @Override
    @Transactional
    public CategoryResponseDTO create(CategoryRequestDTO dto) {
        Category category = categoryMapper.toEntity(dto);

        if (categoryRepository.existsByName(dto.name()))
            throw new DuplicateException("Category already exists");

        categoryRepository.save(category);

        return categoryMapper.toResponse(category);
    }

    @Override
    @Transactional
    public CategoryResponseDTO update(
            Long id,
            CategoryRequestDTO dto) {
        Category category = getCategoryOrElseThrow(id);

        if (!category.getName().equals(dto.name()) && categoryRepository.existsByName(dto.name()))
            throw new DuplicateException("Category already exists");

        // Verify the current user is the owner or has force update permission
        securityUtils.checkOwnerOrPermission(category.getCreatedBy(), Permission.CATEGORY_FORCE_UPDATE,
                "update this category");

        categoryMapper.updateEntity(category, dto);

        return categoryMapper.toResponse(category);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Category category = getCategoryWithDeletedOrElseThrow(id);

        if (category.getDeletedAt() != null)
            throw new BadRequestException("Category is deleted");

        // Verify the current user is the owner or has force delete permission
        securityUtils.checkOwnerOrPermission(category.getCreatedBy(), Permission.CATEGORY_FORCE_DELETE,
                "delete this category");

        category.softDelete(securityUtils.getCurrentAuditorId());

        categoryRepository.save(category);
    }

    @Override
    @Transactional
    public void restore(Long id) {
        Category category = getCategoryWithDeletedOrElseThrow(id);

        if (category.getDeletedAt() == null)
            throw new BadRequestException("Category is not deleted");

        category.restore();

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
    public PageResponseDTO<CategorySimpleDTO> findAll(
            Pageable pageable) {
        Page<Category> page = categoryRepository.findAll(pageable);

        return categoryMapper.toPageResponse(page);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponseDTO<CategorySimpleDTO> findAllDeleted(
            Pageable pageable) {
        Page<Category> page = categoryRepository.findAllDeleted(pageable);

        return categoryMapper.toPageResponse(page);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponseDTO<CategorySimpleDTO> findAllWithDeleted(
            Pageable pageable) {
        Page<Category> page = categoryRepository.findAllWithDeleted(pageable);

        return categoryMapper.toPageResponse(page);
    }

    // Helper that fetch a category
    private Category getCategoryOrElseThrow(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Category not found"));
    }

    // Helper that fetch a category including soft-deleted
    private Category getCategoryWithDeletedOrElseThrow(Long id) {
        return categoryRepository.findByIdWithDeleted(id)
                .orElseThrow(() -> new NotFoundException("Category not found"));
    }
}
