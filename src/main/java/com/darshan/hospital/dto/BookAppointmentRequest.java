package com.darshan.hospital.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.OffsetDateTime;

public record BookAppointmentRequest(
        @NotNull(message = "Doctor is required")
        Long doctorId,

        @NotNull(message = "Start time is required")
        OffsetDateTime startTime,

        @Size(max = 500, message = "Reason must be at most 500 characters")
        String reason
) {
}