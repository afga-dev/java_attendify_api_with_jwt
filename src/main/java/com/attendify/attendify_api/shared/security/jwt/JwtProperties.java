package com.attendify.attendify_api.shared.security.jwt;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "application.security.jwt")
public class JwtProperties {
    @NotBlank
    private String secretKey;

    @NotNull
    private Long accessExpirationMs;

    @NotNull
    private Long refreshExpirationMs;
}
