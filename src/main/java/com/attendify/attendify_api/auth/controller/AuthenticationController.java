package com.attendify.attendify_api.auth.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.attendify.attendify_api.auth.dto.AuthenticationResponseDTO;
import com.attendify.attendify_api.auth.dto.ChangeEmailRequestDTO;
import com.attendify.attendify_api.auth.dto.ChangePasswordRequestDTO;
import com.attendify.attendify_api.auth.dto.LoginRequestDTO;
import com.attendify.attendify_api.auth.dto.RegisterAdminRequestDTO;
import com.attendify.attendify_api.auth.dto.RegisterRequestDTO;
import com.attendify.attendify_api.auth.service.AuthenticationService;
import com.attendify.attendify_api.shared.annotation.role.AdminOnly;
import com.attendify.attendify_api.shared.annotation.user.CanChangeEmailUser;
import com.attendify.attendify_api.shared.annotation.user.CanChangePasswordUser;
import com.attendify.attendify_api.shared.exception.ErrorResponse;
import com.attendify.attendify_api.shared.jwt.SecurityConstants;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/attendify/v1/auth")
@RequiredArgsConstructor
public class AuthenticationController {
    private final AuthenticationService authenticationService;

    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponseDTO> register(@Valid @RequestBody RegisterRequestDTO dto) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(authenticationService.register(dto));
    }

    @PostMapping("/register/admin")
    @AdminOnly
    public ResponseEntity<AuthenticationResponseDTO> registerByAdmin(
            @Valid @RequestBody RegisterAdminRequestDTO request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(authenticationService.registerByAdmin(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponseDTO> login(@Valid @RequestBody LoginRequestDTO dto) {
        return ResponseEntity.ok(authenticationService.login(dto));
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(HttpServletRequest request) {
        String authHeader = request.getHeader(SecurityConstants.AUTHORIZATION_HEADER);

        AuthenticationResponseDTO authResponse = authenticationService.refresh(authHeader);

        return ResponseEntity.ok(authResponse);
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        String authHeader = request.getHeader(SecurityConstants.AUTHORIZATION_HEADER);

        var result = authenticationService.logout(authHeader);

        switch (result) {
            case SUCCESS -> {
                SecurityContextHolder.clearContext();
                return ResponseEntity.ok().body("Logout successful");
            }
            case TOKEN_NOT_FOUND -> {
                return buildResponse(HttpStatus.NOT_FOUND,
                        "No active session was found for the provided token");
            }
            case ALREADY_REVOKED -> {
                return buildResponse(HttpStatus.CONFLICT,
                        "The token has already been revoked or expired");
            }
            case INVALID_HEADER -> {
                return buildResponse(HttpStatus.BAD_REQUEST,
                        "Missing or malformed authorization header");
            }
        }

        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error");
    }

    @PostMapping("/password")
    @CanChangePasswordUser
    public ResponseEntity<?> changePassword(@Valid @RequestBody ChangePasswordRequestDTO dto) {
        var result = authenticationService.changePassword(dto);

        switch (result) {
            case SUCCESS -> {
                SecurityContextHolder.clearContext();
                return ResponseEntity.ok().body("Change password successful");
            }
            case OLD_DO_NOT_MATCH -> {
                return buildResponse(HttpStatus.CONFLICT,
                        "Current password is incorrect");
            }
            case NEW_MATCH_OLD -> {
                return buildResponse(HttpStatus.CONFLICT,
                        "New password must be different");
            }
            case INVALID_HEADER -> {
                return buildResponse(HttpStatus.BAD_REQUEST,
                        "Missing or malformed authorization header");
            }
        }

        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error");
    }

    @PostMapping("/email")
    @CanChangeEmailUser
    public ResponseEntity<?> changeEmail(@Valid @RequestBody ChangeEmailRequestDTO dto) {
        var result = authenticationService.changeEmail(dto);

        switch (result) {
            case SUCCESS -> {
                SecurityContextHolder.clearContext();
                return ResponseEntity.ok().body("Change email successful");
            }
            case OLD_DO_NOT_MATCH -> {
                return buildResponse(HttpStatus.CONFLICT,
                        "Current email is incorrect");
            }
            case NEW_MATCH_OLD -> {
                return buildResponse(HttpStatus.CONFLICT,
                        "New email must be different");
            }
            case INVALID_HEADER -> {
                return buildResponse(HttpStatus.BAD_REQUEST,
                        "Missing or malformed authorization header");
            }
        }

        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error");
    }

    private ResponseEntity<ErrorResponse> buildResponse(HttpStatus status, String message) {
        ErrorResponse body = ErrorResponse.of(status.value(), status.getReasonPhrase(), message);
        return ResponseEntity.status(status).body(body);
    }
}
