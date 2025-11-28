package com.attendify.attendify_api.user.model.enums;

import java.util.Set;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Role {
    USER(Set.of(
            Permission.USER_READ,
            Permission.USER_PASSWORD,
            Permission.USER_EMAIL,
            Permission.USER_DELETE,

            Permission.CATEGORY_READ,

            Permission.EVENT_READ,

            Permission.EVENT_REGISTRATION_READ,
            Permission.EVENT_REGISTRATION_CREATE,
            Permission.EVENT_REGISTRATION_UPDATE,
            Permission.EVENT_REGISTRATION_DELETE)),

    MANAGER(Set.of(
            Permission.USER_READ,
            Permission.USER_PASSWORD,
            Permission.USER_EMAIL,
            Permission.USER_DELETE,

            Permission.CATEGORY_READ,
            Permission.CATEGORY_CREATE,
            Permission.CATEGORY_UPDATE,
            Permission.CATEGORY_DELETE,

            Permission.EVENT_READ,
            Permission.EVENT_CREATE,
            Permission.EVENT_UPDATE,
            Permission.EVENT_DELETE,

            Permission.EVENT_REGISTRATION_READ,
            Permission.EVENT_REGISTRATION_CREATE,
            Permission.EVENT_REGISTRATION_UPDATE,
            Permission.EVENT_REGISTRATION_DELETE)),

    ADMIN((Set.of(
            Permission.values())));

    private final Set<Permission> permissions;
}
