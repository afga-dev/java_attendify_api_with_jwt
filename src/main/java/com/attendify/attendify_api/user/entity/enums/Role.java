package com.attendify.attendify_api.user.entity.enums;

import java.util.EnumSet;
import java.util.Set;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Role {
    USER(Set.of(
            Permission.EVENT_REGISTRATION_CREATE,
            Permission.EVENT_REGISTRATION_DELETE)),

    MANAGER(Set.of(
            Permission.CATEGORY_CREATE,
            Permission.CATEGORY_UPDATE,
            Permission.CATEGORY_DELETE,

            Permission.EVENT_CREATE,
            Permission.EVENT_UPDATE,
            Permission.EVENT_DELETE,

            Permission.EVENT_REGISTRATION_CREATE,
            Permission.EVENT_REGISTRATION_CHECKIN,
            Permission.EVENT_REGISTRATION_DELETE)),

    ADMIN(EnumSet.allOf(Permission.class));

    private final Set<Permission> permissions;
}
