package com.attendify.attendify_api.shared.security.jwt;

import java.util.Date;
import java.util.Map;
import java.util.function.Function;

import javax.crypto.SecretKey;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.attendify.attendify_api.auth.entity.enums.TokenPurpose;
import com.attendify.attendify_api.shared.security.CustomUserDetails;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class JwtService {
    private final JwtProperties jwtProperties;

    // Extracts the subject user ID from a token
    public String extractSubject(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // Extracts a specific claim from the token
    public <T> T extractClaim(String token, Function<Claims, T> claim) {
        final Claims claims = extractAllClaims(token);
        return claim.apply(claims);
    }

    // Generates a token for the given user details with default claims
    public String generateToken(UserDetails userDetails, TokenPurpose tokenPurpose) {
        return generateToken(Map.of(), userDetails, tokenPurpose);
    }

    // Generates a token for the given user details with optional extra claims
    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails, TokenPurpose tokenPurpose) {
        return switch (tokenPurpose) {
            case ACCESS -> buildToken(extraClaims, userDetails, jwtProperties.getAccessExpirationMs());
            case REFRESH -> buildToken(extraClaims, userDetails, jwtProperties.getRefreshExpirationMs());
        };
    }

    // Validates the token against the given user details
    public boolean isTokenValid(String token, UserDetails userDetails) {
        if (!(userDetails instanceof CustomUserDetails customUserDetails)
                || customUserDetails.getId() == null)
            return false;

        final Long tokenUserId;
        try {
            tokenUserId = extractUserId(token);
        } catch (BadCredentialsException ex) {
            return false;
        }

        return tokenUserId.equals(customUserDetails.getId())
                && !isTokenExpired(token);
    }

    // Extracts the user ID from the token
    public Long extractUserId(String token) {
        String subject = extractSubject(token);
        try {
            return Long.valueOf(subject);
        } catch (NumberFormatException ex) {
            throw new BadCredentialsException("Invalid token subject");
        }
    }

    // Extracts all claims from the token
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSignInKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    // Returns the signing key from the configured secret
    private SecretKey getSignInKey() {
        byte[] keyByte = Decoders.BASE64.decode(jwtProperties.getSecretKey());
        return Keys.hmacShaKeyFor(keyByte);
    }

    // Builds a JWT with claims, subject, issue date, expiration, and signature
    private String buildToken(
            Map<String, Object> extraClaims,
            UserDetails userDetails,
            long expiration) {
        if (!(userDetails instanceof CustomUserDetails customUserDetails) || customUserDetails.getId() == null)
            throw new IllegalArgumentException("JWT generation requires CustomUserDetails with non-null id as subject");

        long now = System.currentTimeMillis();
        Date issuedAt = new Date(now);
        Date expiresAt = new Date(now + expiration);

        String subject = Long.toString(customUserDetails.getId());

        return Jwts.builder()
                .claims(extraClaims)
                .subject(subject)
                .issuedAt(issuedAt)
                .expiration(expiresAt)
                .signWith(getSignInKey(), Jwts.SIG.HS256)
                .compact();
    }

    // Checks if the token is expired
    private boolean isTokenExpired(String token) {
        try {
            Date expiration = extractExpiration(token);
            return expiration.before(new Date());
        } catch (JwtException ex) {
            return true;
        }
    }

    // Extracts the expiration date from the token
    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }
}
