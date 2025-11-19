package com.attendify.attendify_api.shared.dto;

import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PageResponseDTO<T> {
    private List<T> items;
    private int page;
    private int size;
    private long totalItems;
    private long totalPages;
    private boolean isLast;
}
