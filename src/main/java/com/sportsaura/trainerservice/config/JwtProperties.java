package com.sportsaura.trainerservice.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Settings under {@code app.jwt}. The secret must be the same one the user service signs tokens
 * with, otherwise every token is rejected.
 *
 * @param secret shared signing key; at least 32 characters
 */
@Validated
@ConfigurationProperties(prefix = "app.jwt")
public record JwtProperties(
    @NotBlank @Size(min = 32, message = "JWT_SECRET must be at least 32 characters")
        String secret) {}
