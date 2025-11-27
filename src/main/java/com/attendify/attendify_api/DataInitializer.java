package com.attendify.attendify_api;

import java.util.Set;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.attendify.attendify_api.auth.dto.RegisterAdminRequestDTO;
import com.attendify.attendify_api.auth.service.AuthenticationService;
import com.attendify.attendify_api.user.model.Role;
import com.attendify.attendify_api.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class DataInitializer {

    private final AuthenticationService authenticationService;
    private final UserRepository userRepository;

    @Bean
    public CommandLineRunner createInitialAdmin() {
        return args -> {

            if (userRepository.existsByEmail("afga.work.contact@gmail.com"))
                return;

            RegisterAdminRequestDTO request = RegisterAdminRequestDTO.builder()
                    .email("afga.work.contact@gmail.com")
                    .password("te3T_D3v")
                    .roles(Set.of(Role.ADMIN))
                    .build();

            authenticationService.registerByAdmin(request);
        };
    }
}
