package com.attendify.attendify_api.shared.jwt;

import java.util.Date;
import java.util.Map;
import java.util.function.Function;

import javax.crypto.SecretKey;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.attendify.attendify_api.auth.model.enums.TokenPurpose;
import com.attendify.attendify_api.shared.config.JwtProperties;
import com.attendify.attendify_api.user.security.CustomUserDetails;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class JwtService {
    private final JwtProperties jwtProperties;

    public String extractSubject(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claim) {
        final Claims claims = extractAllClaims(token);
        return claim.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSignInKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getSignInKey() {
        byte[] keyByte = Decoders.BASE64.decode(jwtProperties.getSecretKey());
        return Keys.hmacShaKeyFor(keyByte);
    }

    public String generateToken(UserDetails userDetails, TokenPurpose tokenPurpose) {
        return generateToken(Map.of(), userDetails, tokenPurpose);
    }

    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails, TokenPurpose tokenPurpose) {
        return switch (tokenPurpose) {
            case ACCESS -> buildToken(extraClaims, userDetails, jwtProperties.getAccessExpirationMs());
            case REFRESH -> buildToken(extraClaims, userDetails, jwtProperties.getRefreshExpirationMs());
        };
    }

    private String buildToken(
            Map<String, Object> extraClaims,
            UserDetails userDetails,
            long expiration) {
        long now = System.currentTimeMillis();
        Date issuedAt = new Date(now);
        Date expiresAt = new Date(now + expiration);

        String subject = getIdOrElseUsername(userDetails);

        return Jwts.builder()
                .claims(extraClaims)
                .subject(subject)
                .issuedAt(issuedAt)
                .expiration(expiresAt)
                .signWith(getSignInKey(), Jwts.SIG.HS256)
                .compact();
    }

    private String getIdOrElseUsername(UserDetails userDetails) {
        if (userDetails instanceof CustomUserDetails customUserDetails) {
            Long id = customUserDetails.getId();
            return id != null ? id.toString() : userDetails.getUsername();
        }

        return userDetails.getUsername();
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        String tokenSubject = extractSubject(token);

        if (userDetails instanceof CustomUserDetails customUserDetails) {
            Long tokenUserId;

            try {
                tokenUserId = Long.valueOf(tokenSubject);
            } catch (NumberFormatException ex) {
                return false;
            }

            return tokenUserId.equals(customUserDetails.getId()) && !isTokenExpired(token);
        } else {
            return tokenSubject.equals(userDetails.getUsername()) && !isTokenExpired(token);
        }
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }
}
