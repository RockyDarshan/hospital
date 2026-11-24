package com.darshan.hospital.dto;

import com.darshan.hospital.entity.Appointment;
import com.darshan.hospital.entity.AppointmentStatus;
import java.time.OffsetDateTime;
import java.time.ZoneId;

public record AppointmentResponse(
        Long id,
        Long doctorId,
        String doctorName,
        String department,
        Long patientId,
        String patientName,
        OffsetDateTime startTime,
        OffsetDateTime endTime,
        AppointmentStatus status,
        String reason
) {

    public static AppointmentResponse from(Appointment a, ZoneId zone) {
        return new AppointmentResponse(
                a.getId(),
                a.getDoctor().getId(),
                a.getDoctor().getFullName(),
                a.getDoctor().getDepartment().getName(),
                a.getPatient().getId(),
                a.getPatient().getFullName(),
                a.getStartTime().atZone(zone).toOffsetDateTime(),
                a.getEndTime().atZone(zone).toOffsetDateTime(),
                a.getStatus(),
                a.getReason());
    }
}