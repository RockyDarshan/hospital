package com.darshan.hospital.dto;

import com.darshan.hospital.entity.Gender;
import com.darshan.hospital.entity.Patient;
import java.time.LocalDate;

public record PatientResponse(
        Long id,
        String fullName,
        String email,
        LocalDate dateOfBirth,
        Gender gender,
        String phone,
        String address
) {

    public static PatientResponse from(Patient patient) {
        return new PatientResponse(
                patient.getId(),
                patient.getFullName(),
                patient.getUser().getEmail(),
                patient.getDateOfBirth(),
                patient.getGender(),
                patient.getPhone(),
                patient.getAddress());
    }
}