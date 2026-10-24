package com.darshan.hospital.dto;

import com.darshan.hospital.entity.Doctor;

public record DoctorResponse(
        Long id,
        String fullName,
        String email,
        String phone,
        String qualification,
        Long departmentId,
        String departmentName
) {

    public static DoctorResponse from(Doctor doctor) {
        return new DoctorResponse(
                doctor.getId(),
                doctor.getFullName(),
                doctor.getUser().getEmail(),
                doctor.getPhone(),
                doctor.getQualification(),
                doctor.getDepartment().getId(),
                doctor.getDepartment().getName());
    }
}