package com.attendify.attendify_api.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;

@Builder
public record AuthResponseDTO(
        @JsonProperty("access_token") String accessToken,

        @JsonProperty("refresh_token") String refreshToken) {
}
