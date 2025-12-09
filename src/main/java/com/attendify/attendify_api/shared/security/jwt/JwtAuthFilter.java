package com.attendify.attendify_api.shared.security.jwt;

import java.io.IOException;
import java.util.Arrays;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import com.attendify.attendify_api.shared.security.CustomUserDetails;
import com.attendify.attendify_api.shared.security.SecurityConstants;
import com.attendify.attendify_api.user.entity.User;
import com.attendify.attendify_api.user.repository.UserRepository;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    // Paths that should bypass JWT authentication
    private static final String[] SKIP_PATHS = {
            "/attendify/v1/auth/login",
            "/attendify/v1/auth/register",
            "/attendify/v1/auth/refresh",
            "/error"
    };

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        // Skip JWT validation for public paths
        if (shouldSkip(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        final String authHeader = request.getHeader(SecurityConstants.AUTHORIZATION_HEADER);
        if (!hasBearerToken(authHeader)) {
            filterChain.doFilter(request, response);
            return;
        }

        final String jwt = authHeader.substring(SecurityConstants.BEARER_PREFIX.length());

        // Skip if authentication is already set in the security context
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            filterChain.doFilter(request, response);
            return;
        }

        // Extract user ID from JWT, skip filter if invalid
        final Long userId;
        try {
            userId = jwtService.extractUserId(jwt);
        } catch (JwtException | IllegalArgumentException ex) {
            filterChain.doFilter(request, response);
            return;
        }

        // Load user from DB
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            filterChain.doFilter(request, response);
            return;
        }

        CustomUserDetails customUserDetails = new CustomUserDetails(user);

        // Validate JWT against user details
        if (!jwtService.isTokenValid(jwt, customUserDetails)) {
            filterChain.doFilter(request, response);
            return;
        }

        // Set authentication in the security context
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                customUserDetails,
                null,
                customUserDetails.getAuthorities());

        authToken.setDetails(
                new WebAuthenticationDetailsSource().buildDetails(request));

        SecurityContextHolder.getContext().setAuthentication(authToken);
        filterChain.doFilter(request, response);
    }

    // Check if request path should bypass JWT validation
    private boolean shouldSkip(HttpServletRequest request) {
        AntPathMatcher matcher = new AntPathMatcher();
        String path = request.getServletPath();

        return Arrays.stream(SKIP_PATHS)
                .anyMatch(pattern -> matcher.match(pattern, path));
    }

    // Check if Authorization header contains a Bearer token
    private boolean hasBearerToken(String authHeader) {
        return authHeader != null &&
                authHeader.startsWith(SecurityConstants.BEARER_PREFIX);
    }
}
