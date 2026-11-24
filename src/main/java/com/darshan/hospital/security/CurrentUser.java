package com.darshan.hospital.security;

import com.darshan.hospital.entity.Role;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Objects;

// The logged-in user, read from the verified token.
public record CurrentUser(Long id, Role role) {

    public static CurrentUser from(Jwt jwt) {
        return new CurrentUser(
                Long.valueOf(Objects.requireNonNull(jwt.getSubject())),
                Role.valueOf(jwt.getClaimAsString("role")));
    }
}