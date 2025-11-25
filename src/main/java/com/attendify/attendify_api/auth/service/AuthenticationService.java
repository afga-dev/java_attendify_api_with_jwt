package com.attendify.attendify_api.auth.service;

import java.util.Set;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.attendify.attendify_api.auth.dto.AuthenticationResponseDTO;
import com.attendify.attendify_api.auth.dto.LoginRequestDTO;
import com.attendify.attendify_api.auth.dto.RegisterRequestDTO;
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
        if (userRepository.existsByEmail(request.email()))
            throw new IllegalArgumentException("Email '" + request.email() + "'' is already in use");

        User user = User
                .builder()
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .roles(request.roles() != null ? request.roles() : Set.of(Role.USER))
                .build();

        User savedUser = userRepository.save(user);
        var accessToken = jwtService.generateToken(getUser(savedUser), TokenPurpose.ACCESS);
        var refreshToken = jwtService.generateToken(getUser(savedUser), TokenPurpose.REFRESH);

        saveUserToken(savedUser, refreshToken);

        return AuthenticationResponseDTO
                .builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    public AuthenticationResponseDTO login(LoginRequestDTO request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.email(),
                        request.password()));

        User user = userRepository.findByEmail(request.password())
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
            username = jwtService.extractUsername(refreshToken);
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

}
