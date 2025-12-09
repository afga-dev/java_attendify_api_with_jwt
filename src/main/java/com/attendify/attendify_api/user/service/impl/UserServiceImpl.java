package com.attendify.attendify_api.user.service.impl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.attendify.attendify_api.auth.repository.TokenRepository;
import com.attendify.attendify_api.shared.dto.PageResponseDTO;
import com.attendify.attendify_api.shared.exception.BadRequestException;
import com.attendify.attendify_api.shared.exception.NotFoundException;
import com.attendify.attendify_api.shared.security.SecurityUtils;
import com.attendify.attendify_api.user.dto.UserSummaryDTO;
import com.attendify.attendify_api.user.entity.User;
import com.attendify.attendify_api.user.mapper.UserMapper;
import com.attendify.attendify_api.user.repository.UserRepository;
import com.attendify.attendify_api.user.service.UserService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserMapper userMapper;
    private final UserRepository userRepository;
    private final TokenRepository tokenRepository;
    private final SecurityUtils securityUtils;

    @Override
    @Transactional(readOnly = true)
    public UserSummaryDTO getMyUser() {
        User user = securityUtils.getAuthenticatedUser();

        return userMapper.toSummary(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserSummaryDTO getUser(Long id) {
        User user = getUserWithDeletedOrElseThrow(id);

        return userMapper.toSummary(user);
    }

    @Override
    @Transactional
    public void delete() {
        User user = securityUtils.getAuthenticatedUser();

        softDeleteUser(user);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        User user = getUserWithDeletedOrElseThrow(id);

        if (user.getDeletedAt() != null)
            throw new BadRequestException("User is deleted");

        if (user.getId().equals(securityUtils.getAuthenticatedUserId()))
            throw new BadRequestException("You cannot delete your own account via admin method");

        softDeleteUser(user);
    }

    @Override
    @Transactional
    public void restore(Long id) {
        User user = getUserWithDeletedOrElseThrow(id);

        if (user.getDeletedAt() == null)
            throw new BadRequestException("User is not deleted");

        user.restore();

        userRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponseDTO<UserSummaryDTO> findAll(Pageable pageable) {
        Page<User> page = userRepository.findAll(pageable);

        return userMapper.toPageResponse(page);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponseDTO<UserSummaryDTO> findAllDeleted(Pageable pageable) {
        Page<User> page = userRepository.findAllDeleted(pageable);

        return userMapper.toPageResponse(page);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponseDTO<UserSummaryDTO> findAllWithDeleted(Pageable pageable) {
        Page<User> page = userRepository.findAllWithDeleted(pageable);

        return userMapper.toPageResponse(page);
    }

    // Helper that fetch an user including soft-deleted
    private User getUserWithDeletedOrElseThrow(Long id) {
        return userRepository.findByIdWithDeleted(id)
                .orElseThrow(() -> new NotFoundException("User not found"));
    }

    // Helper that performs soft deletion and revokes all tokens for the user
    private void softDeleteUser(User user) {
        user.softDelete(user.getId());
        tokenRepository.revokeAllUserTokens(user.getId());
        userRepository.save(user);
    }
}
