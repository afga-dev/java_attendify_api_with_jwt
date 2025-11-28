package com.attendify.attendify_api.user.mapper;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import com.attendify.attendify_api.shared.dto.PageResponseDTO;
import com.attendify.attendify_api.user.dto.UserSummaryDTO;
import com.attendify.attendify_api.user.model.User;

@Component
public class UserMapper {
    public UserSummaryDTO toSummary(User user) {
        return UserSummaryDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .roles(user.getRoles())
                .build();
    }

    public PageResponseDTO<UserSummaryDTO> toPageResponse(Page<User> page) {
        return PageResponseDTO.<UserSummaryDTO>builder()
                .items(page.getContent().stream()
                        .map(this::toSummary)
                        .toList())
                .page(page.getNumber())
                .size(page.getSize())
                .totalItems(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .isLast(page.isLast())
                .build();
    }
}
