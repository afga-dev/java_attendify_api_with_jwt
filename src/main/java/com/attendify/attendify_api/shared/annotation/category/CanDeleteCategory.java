package com.attendify.attendify_api.shared.annotation.category;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.security.access.prepost.PreAuthorize;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@PreAuthorize("""
            hasAuthority('CATEGORY_DELETE')
            and (
                hasRole('ADMIN')
                or @ownershipService.isOwner(#id, T(com.attendify.attendify_api.event.model.Category), authentication)
            )
        """)
public @interface CanDeleteCategory {
}
