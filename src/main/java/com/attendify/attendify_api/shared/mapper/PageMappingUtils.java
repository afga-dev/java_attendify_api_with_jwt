package com.attendify.attendify_api.shared.mapper;

import java.util.List;
import java.util.function.Function;

import org.springframework.data.domain.Page;

import com.attendify.attendify_api.shared.dto.PageResponseDTO;

// Utility class for mapping Spring Data Page objects to PageResponseDTO
public class PageMappingUtils {
    /*
     * page: the Page of entities from the repository
     * mapper: a function to convert each entity to a DTO
     * <E>: the entity type
     * <D>: the DTO type
     */
    public static <E, D> PageResponseDTO<D> toPageResponse(Page<E> page, Function<E, D> mapper) {
        return PageResponseDTO.<D>builder()
                .items(List.copyOf(
                        page.getContent().stream()
                                .map(mapper)
                                .toList()))
                .page(page.getNumber())
                .size(page.getSize())
                .totalItems(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .isLast(page.isLast())
                .build();
    }
}
