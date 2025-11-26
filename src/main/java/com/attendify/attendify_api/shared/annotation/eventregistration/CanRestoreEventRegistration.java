package com.attendify.attendify_api.shared.annotation.eventregistration;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.security.access.prepost.PreAuthorize;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@PreAuthorize("""
            hasAuthority('EVENT_REGISTRATION_RESTORE')
            and (
                hasRole('ADMIN')
                or @ownershipService.isOwner(#id, T(com.attendify.attendify_api.event.model.EventRegistration), authentication)
            )
        """)
public @interface CanRestoreEventRegistration {
}
