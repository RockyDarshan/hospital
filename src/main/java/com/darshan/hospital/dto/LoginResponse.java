package com.darshan.hospital.dto;

public record LoginResponse(
        String accessToken, String tokenType, long expiresInSeconds, String role) {
}