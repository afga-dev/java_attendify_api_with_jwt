package com.attendify.attendify_api.user.dto;

import java.util.Set;

import com.attendify.attendify_api.user.model.Role;

import lombok.Data;

@Data
public class UserSummaryDTO {
    private Long id;
    private String email;
    private Set<Role> roles;
}
