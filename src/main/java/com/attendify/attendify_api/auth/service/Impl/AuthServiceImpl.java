package com.attendify.attendify_api.auth.service.Impl;

import java.util.Set;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.attendify.attendify_api.auth.dto.AuthResponseDTO;
import com.attendify.attendify_api.auth.dto.ChangeEmailRequestDTO;
import com.attendify.attendify_api.auth.dto.ChangePasswordRequestDTO;
import com.attendify.attendify_api.auth.dto.LoginRequestDTO;
import com.attendify.attendify_api.auth.dto.RegisterAdminRequestDTO;
import com.attendify.attendify_api.auth.dto.RegisterRequestDTO;
import com.attendify.attendify_api.auth.entity.Token;
import com.attendify.attendify_api.auth.entity.enums.TokenPurpose;
import com.attendify.attendify_api.auth.entity.enums.TokenType;
import com.attendify.attendify_api.auth.enums.EmailResult;
import com.attendify.attendify_api.auth.enums.LogoutResult;
import com.attendify.attendify_api.auth.enums.PasswordResult;
import com.attendify.attendify_api.auth.repository.TokenRepository;
import com.attendify.attendify_api.auth.service.AuthService;
import com.attendify.attendify_api.shared.exception.NotFoundException;
import com.attendify.attendify_api.shared.security.CustomUserDetails;
import com.attendify.attendify_api.shared.security.SecurityConstants;
import com.attendify.attendify_api.shared.security.SecurityUtils;
import com.attendify.attendify_api.shared.security.jwt.JwtService;
import com.attendify.attendify_api.user.entity.User;
import com.attendify.attendify_api.user.entity.enums.Role;
import com.attendify.attendify_api.user.repository.UserRepository;

import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final TokenRepository tokenRepository;
    private final SecurityUtils securityUtils;

    @Override
    @Transactional
    public AuthResponseDTO register(RegisterRequestDTO dto) {
        // Standard user registration with default USER role
        return registerInternal(
                dto.email(),
                dto.password(),
                Set.of(Role.USER));
    }

    @Override
    @Transactional
    public AuthResponseDTO registerByAdmin(RegisterAdminRequestDTO dto) {
        // Admin can assign roles explicitly
        return registerInternal(
                dto.email(),
                dto.password(),
                dto.roles());
    }

    @Override
    @Transactional
    public AuthResponseDTO login(LoginRequestDTO dto) {
        // Authenticate credentials against Spring Security
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        dto.email(),
                        dto.password()));

        User user = getEmailOrElseThrow(dto.email());

        tokenRepository.revokeAllUserTokens(user.getId());

        return generateTokens(user);
    }

    @Override
    @Transactional
    public AuthResponseDTO refreshToken(String header) {
        String refreshToken = extractToken(header);

        final Long userId;
        try {
            userId = jwtService.extractUserId(refreshToken);
        } catch (JwtException | IllegalArgumentException e) {
            throw new BadCredentialsException("Invalid or expired refresh token");
        }

        var storedToken = tokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new BadCredentialsException("Refresh token not found"));

        if (storedToken.isExpired() || storedToken.isRevoked())
            throw new BadCredentialsException("Refresh token is no longer valid");

        var userEntity = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(
                        "User not found"));

        var userDetails = getUser(userEntity);

        if (!jwtService.isTokenValid(refreshToken, userDetails))
            throw new BadCredentialsException("Refresh token is no longer valid");

        // Revoke old refresh token and persist
        storedToken.setExpired(true);
        storedToken.setRevoked(true);
        tokenRepository.save(storedToken);

        return generateTokens(userEntity);
    }

    @Override
    @Transactional
    public LogoutResult logout(String header) {
        final String jwt = extractToken(header);

        try {
            jwtService.extractUserId(jwt);
        } catch (JwtException | IllegalArgumentException e) {
            return LogoutResult.TOKEN_NOT_FOUND;
        }

        var refreshTokenOpt = tokenRepository.findByToken(jwt);
        if (refreshTokenOpt.isEmpty())
            return LogoutResult.TOKEN_NOT_FOUND;

        var refreshToken = refreshTokenOpt.get();
        if (refreshToken.isExpired() || refreshToken.isRevoked())
            return LogoutResult.ALREADY_REVOKED;

        // Revoke token to prevent further use
        refreshToken.setRevoked(true);
        refreshToken.setExpired(true);
        tokenRepository.save(refreshToken);

        return LogoutResult.SUCCESS;
    }

    @Override
    @Transactional
    public PasswordResult changePassword(ChangePasswordRequestDTO dto) {
        User user = securityUtils.getAuthenticatedUser();

        if (!passwordEncoder.matches(dto.currentPassword(), user.getPassword()))
            return PasswordResult.OLD_DO_NOT_MATCH;

        if (passwordEncoder.matches(dto.newPassword(), user.getPassword()))
            return PasswordResult.NEW_MATCH_OLD;

        // Update password securely
        user.setPassword(passwordEncoder.encode(dto.newPassword()));
        userRepository.save(user);

        // Revoke all tokens to enforce re-authentication
        tokenRepository.revokeAllUserTokens(user.getId());

        return PasswordResult.SUCCESS;
    }

    @Override
    @Transactional
    public EmailResult changeEmail(ChangeEmailRequestDTO dto) {
        User user = securityUtils.getAuthenticatedUser();

        if (!dto.currentEmail().equals(user.getEmail()))
            return EmailResult.OLD_DO_NOT_MATCH;

        if (dto.newEmail().equals(user.getEmail()))
            return EmailResult.NEW_MATCH_OLD;

        if (userRepository.existsByEmail(dto.newEmail()))
            return EmailResult.ALREADY_EXISTS;

        // Update email
        user.setEmail(dto.newEmail());
        userRepository.save(user);

        // Revoke all tokens to enforce re-authentication
        tokenRepository.revokeAllUserTokens(user.getId());

        return EmailResult.SUCCESS;
    }

    // Internal helper to handle both user and admin registration
    private AuthResponseDTO registerInternal(
            String email,
            String rawPassword,
            Set<Role> roles) {
        if (userRepository.existsByEmail(email))
            throw new IllegalArgumentException("Email is already in use");

        if (roles == null || roles.isEmpty())
            throw new IllegalArgumentException("User must have at least one role");

        User user = User.builder()
                .email(email)
                .password(passwordEncoder.encode(rawPassword))
                .roles(roles)
                .build();

        User savedUser = userRepository.save(user);

        return generateTokens(savedUser);
    }

    private User getEmailOrElseThrow(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));
    }

    private CustomUserDetails getUser(User user) {
        return new CustomUserDetails(user);
    }

    // Save refresh token
    private void saveUserToken(User user, String jwt) {
        var token = Token
                .builder()
                .user(user)
                .token(jwt)
                .tokenType(TokenType.BEARER)
                .tokenPurpose(TokenPurpose.REFRESH)
                .expired(false)
                .revoked(false)
                .build();
        tokenRepository.save(token);
    }

    // Generate both access and refresh tokens
    private AuthResponseDTO generateTokens(User user) {
        var accessToken = jwtService.generateToken(getUser(user), TokenPurpose.ACCESS);
        var refreshToken = jwtService.generateToken(getUser(user), TokenPurpose.REFRESH);

        saveUserToken(user, refreshToken);

        return new AuthResponseDTO(accessToken, refreshToken);
    }

    // Extracts JWT from Authorization header
    private String extractToken(String header) {
        if (header == null || !header.startsWith(SecurityConstants.BEARER_PREFIX))
            throw new BadCredentialsException("Missing or malformed authorization header");

        return header.substring(SecurityConstants.BEARER_PREFIX.length());
    }
}
