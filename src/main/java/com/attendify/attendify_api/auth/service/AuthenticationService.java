package com.attendify.attendify_api.auth.service;

import java.util.Set;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.attendify.attendify_api.auth.dto.AuthenticationResponseDTO;
import com.attendify.attendify_api.auth.dto.ChangeEmailRequestDTO;
import com.attendify.attendify_api.auth.dto.ChangePasswordRequestDTO;
import com.attendify.attendify_api.auth.dto.LoginRequestDTO;
import com.attendify.attendify_api.auth.dto.RegisterAdminRequestDTO;
import com.attendify.attendify_api.auth.dto.RegisterRequestDTO;
import com.attendify.attendify_api.auth.model.ChangeResult;
import com.attendify.attendify_api.auth.model.LogoutResult;
import com.attendify.attendify_api.auth.model.Token;
import com.attendify.attendify_api.auth.model.TokenPurpose;
import com.attendify.attendify_api.auth.model.TokenType;
import com.attendify.attendify_api.auth.repository.TokenRepository;
import com.attendify.attendify_api.shared.jwt.JwtService;
import com.attendify.attendify_api.shared.jwt.SecurityConstants;
import com.attendify.attendify_api.user.model.Role;
import com.attendify.attendify_api.user.model.User;
import com.attendify.attendify_api.user.repository.UserRepository;
import com.attendify.attendify_api.user.security.CustomUserDetails;

import io.jsonwebtoken.JwtException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final TokenRepository tokenRepository;

    public AuthenticationResponseDTO register(RegisterRequestDTO request) {
        return registerInternal(
                request.email(),
                request.password(),
                Set.of(Role.USER));
    }

    public AuthenticationResponseDTO registerByAdmin(RegisterAdminRequestDTO request) {
        return registerInternal(
                request.email(),
                request.password(),
                request.roles());
    }

    private AuthenticationResponseDTO registerInternal(
            String email,
            String rawPassword,
            Set<Role> roles) {
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email '" + email + "' is already in use");
        }

        User user = User.builder()
                .email(email)
                .password(passwordEncoder.encode(rawPassword))
                .roles(roles)
                .build();

        User savedUser = userRepository.save(user);

        var accessToken = jwtService.generateToken(getUser(savedUser), TokenPurpose.ACCESS);
        var refreshToken = jwtService.generateToken(getUser(savedUser), TokenPurpose.REFRESH);

        saveUserToken(savedUser, refreshToken);

        return AuthenticationResponseDTO.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    public AuthenticationResponseDTO login(LoginRequestDTO request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.email(),
                        request.password()));

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new BadCredentialsException(
                        "User not found with email: " + request.email()));
        var accessToken = jwtService.generateToken(getUser(user), TokenPurpose.ACCESS);
        var refreshToken = jwtService.generateToken(getUser(user), TokenPurpose.REFRESH);

        tokenRepository.revokeAllUserTokens(user.getId());
        saveUserToken(user, refreshToken);

        return AuthenticationResponseDTO
                .builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();

    }

    private CustomUserDetails getUser(User user) {
        return new CustomUserDetails(user);
    }

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

    public AuthenticationResponseDTO refresh(String authHeader) {
        if (authHeader == null || !authHeader.startsWith(SecurityConstants.BEARER_PREFIX)) {
            throw new BadCredentialsException("Missing or malformed authorization header");
        }

        String refreshToken = authHeader.substring(SecurityConstants.BEARER_PREFIX.length());

        final String username;
        try {
            username = jwtService.extractSubject(refreshToken);
        } catch (JwtException e) {
            throw new BadCredentialsException("Invalid or expired refresh token");
        }

        var storedTokenOpt = tokenRepository.findByToken(refreshToken);
        var storedToken = storedTokenOpt
                .orElseThrow(() -> new BadCredentialsException("Refresh token not found"));

        var userEntity = userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "User not found with email: " + username));
        var userDetails = getUser(userEntity);

        if (storedToken.getExpired() || storedToken.getRevoked()
                || !jwtService.isTokenValid(refreshToken, userDetails)) {
            throw new BadCredentialsException("Refresh token is no longer valid");
        }

        storedToken.setExpired(true);
        storedToken.setRevoked(true);
        tokenRepository.save(storedToken);

        String newAccessToken = jwtService.generateToken(userDetails, TokenPurpose.ACCESS);
        String newRefreshToken = jwtService.generateToken(userDetails, TokenPurpose.REFRESH);
        saveUserToken(userEntity, newRefreshToken);

        return AuthenticationResponseDTO.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .build();
    }

    @Transactional
    public LogoutResult logout(String authHeader) {
        if (authHeader == null || !authHeader.startsWith(SecurityConstants.BEARER_PREFIX))
            return LogoutResult.INVALID_HEADER;

        final String jwt = authHeader.substring(SecurityConstants.BEARER_PREFIX.length());

        var refreshTokenOpt = tokenRepository.findByToken(jwt);
        if (refreshTokenOpt.isEmpty())
            return LogoutResult.TOKEN_NOT_FOUND;

        var refreshToken = refreshTokenOpt.get();
        if (refreshToken.getRevoked() || refreshToken.getExpired())
            return LogoutResult.ALREADY_REVOKED;

        refreshToken.setRevoked(true);
        refreshToken.setExpired(true);
        tokenRepository.save(refreshToken);

        return LogoutResult.SUCCESS;
    }

    @Transactional
    public ChangeResult changePassword(ChangePasswordRequestDTO dto) {
        User user = getAuthenticatedUser();

        if (!passwordEncoder.matches(dto.currentPassword(), user.getPassword()))
            return ChangeResult.OLD_DO_NOT_MATCH;

        if (passwordEncoder.matches(dto.newPassword(), user.getPassword()))
            return ChangeResult.NEW_MATCH_OLD;

        user.setPassword(passwordEncoder.encode(dto.newPassword()));
        userRepository.save(user);

        tokenRepository.revokeAllUserTokens(user.getId());

        return ChangeResult.SUCCESS;
    }

    @Transactional
    public ChangeResult changeEmail(ChangeEmailRequestDTO dto) {
        User user = getAuthenticatedUser();

        if (!dto.currentEmail().equals(user.getEmail()))
            return ChangeResult.OLD_DO_NOT_MATCH;

        if (dto.newEmail().equals(user.getEmail()))
            return ChangeResult.NEW_MATCH_OLD;

        user.setEmail(dto.newEmail());
        userRepository.save(user);

        tokenRepository.revokeAllUserTokens(user.getId());

        return ChangeResult.SUCCESS;
    }

    private User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated())
            throw new IllegalStateException("Unauthenticated request");

        CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();

        return customUserDetails.getUser();
    }

}
