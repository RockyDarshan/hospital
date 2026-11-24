package com.darshan.hospital.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

@Component
public class RestSecurityErrorHandler implements AuthenticationEntryPoint, AccessDeniedHandler {

    // 401: no token, or a token that is invalid or expired
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException ex) throws IOException {
        write(response, HttpStatus.UNAUTHORIZED,
                "Authentication is required or the token is invalid.");
    }

    // 403: a valid token, but this user is not allowed to do this
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException ex) throws IOException {
        write(response, HttpStatus.FORBIDDEN,
                "You do not have permission to do this.");
    }

    private void write(HttpServletResponse response, HttpStatus status, String message)
            throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(String.format(
                "{\"timestamp\":\"%s\",\"status\":%d,\"error\":\"%s\",\"message\":\"%s\",\"details\":{}}",
                Instant.now(), status.value(), status.getReasonPhrase(), message));
    }
}