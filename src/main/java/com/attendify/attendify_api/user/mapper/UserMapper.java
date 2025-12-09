package com.attendify.attendify_api.user.mapper;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import com.attendify.attendify_api.shared.dto.PageResponseDTO;
import com.attendify.attendify_api.shared.mapper.PageMappingUtils;
import com.attendify.attendify_api.user.dto.UserSummaryDTO;
import com.attendify.attendify_api.user.entity.User;

@Component
public class UserMapper {
    // Converts entity to simplified DTO
    public UserSummaryDTO toSummary(User user) {
        return UserSummaryDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .roles(user.getRoles())
                .build();
    }

    // Converts a Spring Page of entities to a standardized paginated response
    public PageResponseDTO<UserSummaryDTO> toPageResponse(Page<User> page) {
        return PageMappingUtils.toPageResponse(page, this::toSummary);
    }
}
