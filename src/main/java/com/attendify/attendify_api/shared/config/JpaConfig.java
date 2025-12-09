package com.attendify.attendify_api.shared.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import com.attendify.attendify_api.shared.audit.SecurityAuditorAware;

@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
// Enables JPA auditing
public class JpaConfig {
    @Bean
    public AuditorAware<Long> auditorProvider() {
        return new SecurityAuditorAware();
    }
}
