package com.attendify.attendify_api.auth.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.attendify.attendify_api.auth.dto.AuthResponseDTO;
import com.attendify.attendify_api.auth.dto.ChangeEmailRequestDTO;
import com.attendify.attendify_api.auth.dto.ChangePasswordRequestDTO;
import com.attendify.attendify_api.auth.dto.LoginRequestDTO;
import com.attendify.attendify_api.auth.dto.RegisterAdminRequestDTO;
import com.attendify.attendify_api.auth.dto.RegisterRequestDTO;
import com.attendify.attendify_api.auth.service.AuthService;
import com.attendify.attendify_api.shared.dto.MessageResponseDTO;
import com.attendify.attendify_api.shared.security.SecurityConstants;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/attendify/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponseDTO> register(@Valid @RequestBody RegisterRequestDTO dto) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(authService.register(dto));
    }

    @PostMapping("/register/admin")
    @PreAuthorize("hasAuthority('USER_FORCE_CREATE')")
    public ResponseEntity<AuthResponseDTO> registerByAdmin(
            @Valid @RequestBody RegisterAdminRequestDTO request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(authService.registerByAdmin(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody LoginRequestDTO dto) {
        return ResponseEntity.ok(authService.login(dto));
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<AuthResponseDTO> refresh(HttpServletRequest request) {
        String authHeader = request.getHeader(SecurityConstants.AUTHORIZATION_HEADER);

        AuthResponseDTO authResponse = authService.refreshToken(authHeader);

        return ResponseEntity.ok(authResponse);
    }

    @PostMapping("/logout")
    public ResponseEntity<MessageResponseDTO> logout(HttpServletRequest request) {
        String authHeader = request.getHeader(SecurityConstants.AUTHORIZATION_HEADER);

        var result = authService.logout(authHeader);

        return switch (result) {
            case SUCCESS -> {
                // Clear any security context stored in the current request
                SecurityContextHolder.clearContext();
                yield ResponseEntity.ok(new MessageResponseDTO("Logout successful"));
            }
            case TOKEN_NOT_FOUND -> buildMessageResponse(HttpStatus.NOT_FOUND,
                    "No active session was found for the provided token");
            case ALREADY_REVOKED -> buildMessageResponse(HttpStatus.CONFLICT,
                    "The token has already been revoked or expired");
        };
    }

    @PostMapping("/password")
    public ResponseEntity<MessageResponseDTO> changePassword(@Valid @RequestBody ChangePasswordRequestDTO dto) {
        var result = authService.changePassword(dto);

        return switch (result) {
            case SUCCESS -> {
                // Forces the user to re-authenticate after the password change
                SecurityContextHolder.clearContext();
                yield ResponseEntity.ok().body(new MessageResponseDTO("Change password successful"));
            }
            case OLD_DO_NOT_MATCH -> buildMessageResponse(HttpStatus.CONFLICT,
                    "Current password is incorrect");

            case NEW_MATCH_OLD -> buildMessageResponse(HttpStatus.CONFLICT,
                    "New password must be different");

            case INVALID_HEADER -> buildMessageResponse(HttpStatus.BAD_REQUEST,
                    "Missing or malformed authorization header");
        };
    }

    @PostMapping("/email")
    public ResponseEntity<MessageResponseDTO> changeEmail(@Valid @RequestBody ChangeEmailRequestDTO dto) {
        var result = authService.changeEmail(dto);

        return switch (result) {
            case SUCCESS -> {
                // Forces the user to re-authenticate after the email change
                SecurityContextHolder.clearContext();
                yield ResponseEntity.ok().body(new MessageResponseDTO("Change email successful"));
            }
            case OLD_DO_NOT_MATCH -> buildMessageResponse(HttpStatus.CONFLICT,
                    "Current email is incorrect");
            case NEW_MATCH_OLD -> buildMessageResponse(HttpStatus.CONFLICT,
                    "New email must be different");
            case ALREADY_EXISTS -> buildMessageResponse(HttpStatus.CONFLICT,
                    "Email is already in use");
            case INVALID_HEADER -> buildMessageResponse(HttpStatus.BAD_REQUEST,
                    "Missing or malformed authorization header");
        };
    }

    // Centralized helper to keep error responses consistent
    private ResponseEntity<MessageResponseDTO> buildMessageResponse(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(new MessageResponseDTO(message));
    }
}
