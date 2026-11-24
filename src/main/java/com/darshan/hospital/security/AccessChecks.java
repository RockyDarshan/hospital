package com.darshan.hospital.security;

import com.darshan.hospital.repository.PatientRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component("access")
public class AccessChecks {

    private final PatientRepository patients;

    public AccessChecks(PatientRepository patients) {
        this.patients = patients;
    }

    public boolean isPatientOwner(Long patientId, Authentication authentication) {
        Long userId = userId(authentication);
        return userId != null && patientId != null
                && patients.existsByIdAndUserId(patientId, userId);
    }

    // The token's "sub" claim is the user id, and Spring exposes it as getName().
    private Long userId(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            return null;
        }
        try {
            return Long.valueOf(authentication.getName());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}