package com.attendify.attendify_api.shared.dto;

import java.util.List;

import lombok.Builder;

@Builder
public record PageResponseDTO<T>(
        List<T> items,
        int page,
        int size,
        long totalItems,
        long totalPages,
        boolean isLast) {
}
