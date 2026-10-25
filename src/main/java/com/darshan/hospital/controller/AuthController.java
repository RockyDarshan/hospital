package com.darshan.hospital.controller;

import com.darshan.hospital.dto.LoginRequest;
import com.darshan.hospital.dto.LoginResponse;
import com.darshan.hospital.dto.MeResponse;
import com.darshan.hospital.dto.PatientResponse;
import com.darshan.hospital.dto.RegisterPatientRequest;
import com.darshan.hospital.service.AuthService;
import com.darshan.hospital.service.PatientService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.Objects;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final PatientService patientService;

    public AuthController(AuthService authService, PatientService patientService) {
        this.authService = authService;
        this.patientService = patientService;
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    // Patients register themselves. Doctors are created by an admin.
    @PostMapping("/register")
    public ResponseEntity<PatientResponse> register(
            @Valid @RequestBody RegisterPatientRequest request) {
        PatientResponse created = patientService.register(request);
        return ResponseEntity.created(URI.create("/api/patients/" + created.id())).body(created);
    }

    @GetMapping("/me")
    public MeResponse me(@AuthenticationPrincipal Jwt jwt) {
        return new MeResponse(
                Long.valueOf(Objects.requireNonNull(jwt.getSubject())),
                jwt.getClaimAsString("email"),
                jwt.getClaimAsString("role"));
    }
}