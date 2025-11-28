package com.attendify.attendify_api.user.service.impl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.attendify.attendify_api.auth.repository.TokenRepository;
import com.attendify.attendify_api.shared.dto.PageResponseDTO;
import com.attendify.attendify_api.shared.exception.NotFoundException;
import com.attendify.attendify_api.user.dto.UserSummaryDTO;
import com.attendify.attendify_api.user.mapper.UserMapper;
import com.attendify.attendify_api.user.model.User;
import com.attendify.attendify_api.user.repository.UserRepository;
import com.attendify.attendify_api.user.security.CustomUserDetails;
import com.attendify.attendify_api.user.service.UserService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserMapper userMapper;
    private final UserRepository userRepository;
    private final TokenRepository tokenRepository;

    @Override
    public UserSummaryDTO getMyUser() {
        Long userId = getAuthenticatedUserId();

        User user = getUserOrElseThrow(userId);

        return userMapper.toSummary(user);
    }

    @Override
    public void delete() {
        Long userId = getAuthenticatedUserId();

        User user = getUserOrElseThrow(userId);

        user.softDelete(userId);

        tokenRepository.revokeAllUserTokens(user.getId());

        userRepository.save(user);
    }

    @Override
    public void delete(Long id) {
        User user = getUserIncludingDeletedOrElseThrow(id);

        if (user.getDeleteAt() != null)
            throw new IllegalStateException("User is deleted");

        user.softDelete(id);

        tokenRepository.revokeAllUserTokens(user.getId());

        userRepository.save(user);
    }

    @Override
    public void restore(Long id) {
        User user = getUserIncludingDeletedOrElseThrow(id);

        if (user.getDeleteAt() == null)
            throw new IllegalStateException("User is not deleted");

        user.restore();

        userRepository.save(user);
    }

    @Override
    public PageResponseDTO<UserSummaryDTO> findAll(Pageable pageable) {
        Page<User> page = userRepository.findAll(pageable);

        return userMapper.toPageResponse(page);
    }

    @Override
    public PageResponseDTO<UserSummaryDTO> findAllDeleted(Pageable pageable) {
        Page<User> page = userRepository.findAllDeleted(pageable);

        return userMapper.toPageResponse(page);
    }

    @Override
    public PageResponseDTO<UserSummaryDTO> findAllIncludingDeleted(Pageable pageable) {
        Page<User> page = userRepository.findAllIncludingDeleted(pageable);

        return userMapper.toPageResponse(page);
    }

    private Long getAuthenticatedUserId() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        var principal = (CustomUserDetails) authentication.getPrincipal();
        return principal.getId();
    }

    private User getUserOrElseThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User with id '" + id + "' not found"));
    }

    private User getUserIncludingDeletedOrElseThrow(Long id) {
        return userRepository.findByIdAll(id)
                .orElseThrow(() -> new NotFoundException("User with id '" + id + "' not found"));
    }
}
