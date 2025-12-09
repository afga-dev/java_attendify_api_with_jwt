package com.attendify.attendify_api.auth.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.attendify.attendify_api.auth.entity.Token;

import jakarta.transaction.Transactional;

@Repository
public interface TokenRepository extends JpaRepository<Token, Long> {
    Optional<Token> findByToken(String token);

    // Fetch all non-expired and non-revoked tokens for a specific user
    @Query("""
                SELECT t FROM Token t
                WHERE t.user.id = :id
                AND t.expired = false
                AND t.revoked = false
            """)
    List<Token> findAllValidTokensByUser(@Param("id") Long id);

    // Revoke all active refresh tokens for a specific user
    @Modifying
    @Transactional
    @Query("""
                UPDATE Token t
                SET t.expired = true, t.revoked = true
                WHERE t.user.id = :id
                AND t.tokenPurpose = 'REFRESH'
                AND t.expired = false
                AND t.revoked = false
            """)
    void revokeAllUserTokens(@Param("id") Long id);
}
