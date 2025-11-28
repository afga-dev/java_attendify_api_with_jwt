package com.attendify.attendify_api.user.dto;

import java.util.Set;

import com.attendify.attendify_api.user.model.enums.Role;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserSummaryDTO {
    private Long id;
    private String email;
    private Set<Role> roles;
}
