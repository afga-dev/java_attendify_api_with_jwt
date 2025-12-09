package com.attendify.attendify_api;

import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.attendify.attendify_api.auth.dto.RegisterAdminRequestDTO;
import com.attendify.attendify_api.auth.service.AuthService;
import com.attendify.attendify_api.user.entity.enums.Role;
import com.attendify.attendify_api.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class DataInitializer {
    private final AuthService authService;
    private final UserRepository userRepository;

    @Value("${application.bootstrap.admin.email}")
    private String adminEmail;

    @Value("${application.bootstrap.admin.password}")
    private String adminPassword;

    @Value("${application.bootstrap.admin.enabled:false}")
    private boolean enabled;

    @Bean
    public CommandLineRunner createInitialAdmin() {
        return args -> {

            // Skip initialization if the feature is disabled
            if (!enabled)
                return;

            // Skip if an admin with the configured email already exists
            if (userRepository.existsByEmail(adminEmail))
                return;

            // Prepare and register the admin user
            RegisterAdminRequestDTO request = RegisterAdminRequestDTO.builder()
                    .email(adminEmail)
                    .password(adminPassword)
                    .roles(Set.of(Role.ADMIN))
                    .build();

            authService.registerByAdmin(request);
        };
    }
}
