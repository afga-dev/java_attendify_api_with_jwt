package com.attendify.attendify_api.user.service;

import org.springframework.data.domain.Pageable;

import com.attendify.attendify_api.shared.dto.PageResponseDTO;
import com.attendify.attendify_api.user.dto.UserSummaryDTO;

public interface UserService {
    UserSummaryDTO getMyUser();

    void delete();

    void delete(Long id);

    void restore(Long id);

    PageResponseDTO<UserSummaryDTO> findAll(Pageable pageable);

    PageResponseDTO<UserSummaryDTO> findAllDeleted(Pageable pageable);

    PageResponseDTO<UserSummaryDTO> findAllIncludingDeleted(Pageable pageable);
}
