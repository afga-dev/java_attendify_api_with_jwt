package com.attendify.attendify_api.shared.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;

import com.attendify.attendify_api.shared.jwt.JwtAuthenticationFilter;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
        private final JwtAuthenticationFilter jwtAuthenticationFilter;
        private final AuthenticationProvider authenticationProvider;

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
                return httpSecurity
                                .csrf(csrf -> csrf.disable())
                                .cors(cors -> cors.configurationSource(request -> {
                                        var corsConfiguration = new CorsConfiguration();
                                        corsConfiguration.setAllowedOrigins(List.of(
                                                        "http://localhost:4200",
                                                        "http://localhost:5173",
                                                        "http://localhost:3000"));
                                        corsConfiguration.setAllowedMethods(
                                                        List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
                                        corsConfiguration.setAllowedHeaders(List.of("*"));
                                        corsConfiguration.setAllowCredentials(false);
                                        return corsConfiguration;
                                }))
                                .authorizeHttpRequests(auth -> auth
                                                .requestMatchers("/attendify/v1/auth/**")
                                                .permitAll()
                                                .anyRequest()
                                                .authenticated())
                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                                .authenticationProvider(authenticationProvider)
                                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                                .exceptionHandling(ex -> ex
                                                .authenticationEntryPoint((request, response, e) -> response.sendError(
                                                                HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized"))
                                                .accessDeniedHandler((request, response, e) -> response.sendError(
                                                                HttpServletResponse.SC_FORBIDDEN, "Forbidden")))
                                .build();
        }
}
