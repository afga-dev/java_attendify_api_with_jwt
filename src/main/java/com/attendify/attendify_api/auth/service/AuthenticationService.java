package com.attendify.attendify_api.auth.service;

import java.util.Set;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.attendify.attendify_api.auth.dto.AuthenticationResponse;
import com.attendify.attendify_api.auth.dto.LoginRequest;
import com.attendify.attendify_api.auth.dto.RegisterRequest;
import com.attendify.attendify_api.shared.jwt.JwtService;
import com.attendify.attendify_api.user.model.Role;
import com.attendify.attendify_api.user.model.User;
import com.attendify.attendify_api.user.repository.UserRepository;
import com.attendify.attendify_api.user.security.CustomUserDetails;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
        private final UserRepository userRepository;
        private final PasswordEncoder passwordEncoder;
        private final JwtService jwtService;
        private final AuthenticationManager authenticationManager;

        public AuthenticationResponse register(RegisterRequest request) {
                User user = User
                                .builder()
                                .email(request.getEmail())
                                .password(passwordEncoder.encode(request.getPassword()))
                                .roles(request.getRoles() != null ? request.getRoles() : Set.of(Role.USER))
                                .build();

                User savedUser = userRepository.save(user);
                var jwt = jwtService.generateToken(getUser(savedUser));

                return AuthenticationResponse
                                .builder()
                                .accessToken(jwt)
                                .build();
        }

        public AuthenticationResponse login(LoginRequest request) {
                authenticationManager.authenticate(
                                new UsernamePasswordAuthenticationToken(
                                                request.getEmail(),
                                                request.getPassword()));

                User user = userRepository.findByEmail(request.getEmail())
                                .orElseThrow(() -> new BadCredentialsException(
                                                "User not found with email: " + request.getEmail()));
                var jwt = jwtService.generateToken(getUser(user));

                return AuthenticationResponse
                                .builder()
                                .accessToken(jwt)
                                .build();

        }

        public CustomUserDetails getUser(User user) {
                return new CustomUserDetails(user);
        }
}
