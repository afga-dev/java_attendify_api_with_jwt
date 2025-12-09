package com.attendify.attendify_api.user.dto;

import java.util.Set;

import com.attendify.attendify_api.user.entity.enums.Role;

import lombok.Builder;

@Builder
public record UserSummaryDTO(
        Long id,
        String email,
        Set<Role> roles) {
}
