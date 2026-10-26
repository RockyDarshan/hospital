package com.darshan.hospital.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record MedicalRecordRequest(
        @NotBlank(message = "Diagnosis is required")
        @Size(max = 2000, message = "Diagnosis must be at most 2000 characters")
        String diagnosis,

        @Size(max = 2000, message = "Prescription must be at most 2000 characters")
        String prescription,

        @Size(max = 4000, message = "Notes must be at most 4000 characters")
        String notes
) {
}