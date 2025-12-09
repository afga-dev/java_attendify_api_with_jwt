package com.attendify.attendify_api.shared.security;

import org.springframework.data.domain.AuditorAware;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.attendify.attendify_api.user.entity.User;
import com.attendify.attendify_api.user.entity.enums.Permission;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SecurityUtils {
    private final AuditorAware<Long> auditorAware;

    // Returns the ID of the currently authenticated user
    public Long getAuthenticatedUserId() {
        return getPrincipal().getId();
    }

    // Returns the User entity of the currently authenticated user
    public User getAuthenticatedUser() {
        return getPrincipal().getUser();
    }

    // Returns the current user ID from the auditor provider, throws if unauthenticated
    public Long getCurrentAuditorId() {
        return auditorAware.getCurrentAuditor()
                .orElseThrow(() -> new AccessDeniedException("No authenticated user"));
    }

    // Checks if the authenticated user has a specific permission
    public boolean hasPermission(Permission permission) {
        Authentication authentication = getAuthentication();

        if (authentication == null || !authentication.isAuthenticated())
            return false;

        return authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(permission.name()));
    }

    // Checks if the current user is either the owner of an entity or has the specified permission
    public void checkOwnerOrPermission(
            Long entityOwnerId,
            Permission permission,
            String actionName) {
        Long currentAuditorId = getCurrentAuditorId();

        if (entityOwnerId == null)
            throw new AccessDeniedException("Resource has no owner");

        boolean hasPermission = hasPermission(permission);

        if (!hasPermission && !currentAuditorId.equals(entityOwnerId))
            throw new AccessDeniedException("You are not allowed to " + actionName);
    }

    // Retrieves the authenticated principal as a CustomUserDetails object
    private CustomUserDetails getPrincipal() {
        Authentication authentication = getAuthentication();

        if (authentication == null || !authentication.isAuthenticated())
            throw new IllegalStateException("Unauthenticated request");

        Object principal = authentication.getPrincipal();

        if (!(principal instanceof CustomUserDetails userDetails))
            throw new IllegalStateException("Invalid authentication principal");

        return userDetails;
    }

    // Get the current Authentication object from the security context
    private Authentication getAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }
}
