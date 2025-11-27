package com.attendify.attendify_api.shared.jwt;

import java.io.IOException;
import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.attendify.attendify_api.user.model.User;
import com.attendify.attendify_api.user.repository.UserRepository;
import com.attendify.attendify_api.user.security.CustomUserDetails;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    private static final List<String> PUBLIC_ENDPOINTS = List.of(
            "/attendify/v1/auth/login",
            "/attendify/v1/auth/register");

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        if (PUBLIC_ENDPOINTS.contains(request.getServletPath())) {
            filterChain.doFilter(request, response);
            return;
        }

        final String authHeader = request.getHeader(SecurityConstants.AUTHORIZATION_HEADER);
        if (authHeader == null || !authHeader.startsWith(SecurityConstants.BEARER_PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }

        final String jwt = authHeader.substring(SecurityConstants.BEARER_PREFIX.length());

        String subject;
        try {
            subject = jwtService.extractSubject(jwt);
        } catch (JwtException e) {
            filterChain.doFilter(request, response);
            return;
        }

        if (subject != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            Long userId;
            try {
                userId = Long.valueOf(subject);
            } catch (NumberFormatException ex) {
                filterChain.doFilter(request, response);
                return;
            }

            User user = userRepository.findById(userId).orElse(null);
            if (user == null) {
                filterChain.doFilter(request, response);
                return;
            }

            CustomUserDetails customUserDetails = new CustomUserDetails(user);

            if (jwtService.isTokenValid(jwt, customUserDetails)) {
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        customUserDetails,
                        null,
                        customUserDetails.getAuthorities());

                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        filterChain.doFilter(request, response);
    }
}
