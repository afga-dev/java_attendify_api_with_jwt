package com.attendify.attendify_api.event.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CategoryRequestDTO {
    @NotBlank
    private String name;

    @NotBlank
    private String description;
}
