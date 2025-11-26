package com.attendify.attendify_api.shared.annotation;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import com.attendify.attendify_api.user.security.CustomUserDetails;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;

@Component("ownershipService")
@RequiredArgsConstructor
public class OwnershipService {
    @PersistenceContext
    private final EntityManager entityManager;

    public boolean isOwner(Long entityId, Class<?> entityClass, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated())
            return false;

        Object principal = authentication.getPrincipal();
        if (!(principal instanceof CustomUserDetails userDetails))
            return false;

        Long currentUserId = userDetails.getId();
        String tableName = entityClass.getSimpleName();
        String idAttribute = getIdAttributeName(entityClass);

        String jpql = String.format("SELECT e.createdBy FROM %s e WHERE e.%s = :id", tableName, idAttribute);
        TypedQuery<Long> query = entityManager.createQuery(jpql, Long.class);
        query.setParameter("id", entityId);

        Long ownerId;
        try {
            ownerId = query.getSingleResult();
        } catch (NoResultException ex) {
            return false;
        }

        return ownerId != null && ownerId.equals(currentUserId);
    }

    private String getIdAttributeName(Class<?> entityClass) {
        try {
            var meta = entityManager.getMetamodel().entity(entityClass);
            return meta.getId(meta.getIdType().getJavaType()).getName();
        } catch (Exception e) {
            return "id";
        }
    }
}
