package com.attendify.attendify_api.shared.audit;

import java.util.Optional;

import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.attendify.attendify_api.shared.security.CustomUserDetails;

// Provides the current authenticated user ID for auditing
public class SecurityAuditorAware implements AuditorAware<Long> {
    @Override
    public Optional<Long> getCurrentAuditor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication.getPrincipal().equals("anonymousUser"))
            return Optional.empty();

        Object principal = authentication.getPrincipal();

        // Extract user ID from CustomUserDetails if available
        if (principal instanceof CustomUserDetails userDetails)
            return Optional.of(userDetails.getId());

        // Fallback for unknown principal types
        if (principal instanceof String)
            return Optional.empty();

        return Optional.empty();
    }
}
