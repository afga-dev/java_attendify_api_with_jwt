package com.attendify.attendify_api.shared.config;

import java.io.IOException;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;

import com.attendify.attendify_api.shared.exception.ErrorResponse;
import com.attendify.attendify_api.shared.security.jwt.JwtAuthFilter;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableMethodSecurity
public class SecurityConfig {
    private final JwtAuthFilter jwtAuthFilter;
    private final AuthenticationProvider authenticationProvider;
    private final ObjectMapper objectMapper;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        return httpSecurity
                // Disable CSRF because JWT
                .csrf(csrf -> csrf.disable())
                // Configure CORS for frontend
                .cors(cors -> cors.configurationSource(request -> {
                    var corsConfiguration = new CorsConfiguration();
                    corsConfiguration.setAllowedOrigins(List.of(
                            "http://localhost:4200",
                            "http://localhost:5173",
                            "http://localhost:3000"));
                    corsConfiguration.setAllowedMethods(
                            List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
                    corsConfiguration.setAllowedHeaders(List.of("*"));
                    corsConfiguration.setAllowCredentials(true);
                    return corsConfiguration;
                }))
                // Configure endpoint authorization
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints
                        .requestMatchers("/attendify/v1/auth/register", "/attendify/v1/auth/login",
                                "/attendify/v1/auth/refresh-token")
                        .permitAll()
                        .requestMatchers("/error")
                        .permitAll()
                        // All other requests require authentication
                        .anyRequest()
                        .authenticated())
                // Stateless session management (JWT)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // Use a custom authentication provider
                .authenticationProvider(authenticationProvider)
                // Add JWT filter before the username/password filter
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                // Custom handling for authentication and authorization errors
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) -> {
                            buildError(response,
                                    HttpServletResponse.SC_UNAUTHORIZED,
                                    "Unauthorized",
                                    "Invalid or expired token");
                        })
                        .accessDeniedHandler((request, response, e) -> {
                            buildError(response, HttpServletResponse.SC_FORBIDDEN,
                                    "Forbidden",
                                    "You don't have permission to access this resource");
                        }))
                .build();
    }

    // Utility method to build JSON error responses
    private void buildError(
            HttpServletResponse response,
            int status,
            String error,
            String message) throws IOException {
        var body = ErrorResponse.of(status, error, message);

        response.setContentType("application/json");
        response.setStatus(status);
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }
}
