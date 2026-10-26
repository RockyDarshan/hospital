package com.darshan.hospital.dto;

import com.darshan.hospital.entity.MedicalRecord;
import java.time.Instant;

public record MedicalRecordResponse(
        Long id,
        Long appointmentId,
        String diagnosis,
        String prescription,
        String notes,
        Instant createdAt
) {

    public static MedicalRecordResponse from(MedicalRecord r, Long appointmentId) {
        return new MedicalRecordResponse(
                r.getId(), appointmentId, r.getDiagnosis(),
                r.getPrescription(), r.getNotes(), r.getCreatedAt());
    }
}