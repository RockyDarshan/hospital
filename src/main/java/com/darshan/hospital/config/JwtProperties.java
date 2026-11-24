package com.darshan.hospital.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app.jwt")
public record JwtProperties(
        @NotBlank(message = "JWT_SECRET is not set")
        @Size(min = 32, message = "JWT_SECRET must be at least 32 characters")
        String secret,

        @Positive long expirationMinutes,

        @NotBlank String issuer
) {
}