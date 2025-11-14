package com.attendify.attendify_api.auth.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.attendify.attendify_api.auth.dto.AuthenticationResponseDTO;
import com.attendify.attendify_api.auth.dto.LoginRequestDTO;
import com.attendify.attendify_api.auth.dto.RegisterRequestDTO;
import com.attendify.attendify_api.auth.service.AuthenticationService;
import com.attendify.attendify_api.shared.jwt.SecurityConstants;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/attendify/v1/auth")
@RequiredArgsConstructor
public class AuthenticationController {
    private final AuthenticationService authenticationService;

    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponseDTO> register(@Valid @RequestBody RegisterRequestDTO request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(authenticationService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponseDTO> login(@Valid @RequestBody LoginRequestDTO request) {
        return ResponseEntity.ok(authenticationService.login(request));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthenticationResponseDTO> refresh(
            @RequestHeader(SecurityConstants.AUTHORIZATION_HEADER) String authHeader) {
        AuthenticationResponseDTO authResponse = authenticationService.refresh(authHeader);
        return ResponseEntity.ok(authResponse);
    }
}
