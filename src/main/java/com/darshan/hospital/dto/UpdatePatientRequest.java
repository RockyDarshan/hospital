package com.darshan.hospital.dto;

import com.darshan.hospital.entity.Gender;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record UpdatePatientRequest(
        @NotBlank(message = "Full name is required")
        @Size(max = 150, message = "Full name must be at most 150 characters")
        String fullName,

        @NotNull(message = "Date of birth is required")
        @Past(message = "Date of birth must be in the past")
        LocalDate dateOfBirth,

        @NotNull(message = "Gender is required")
        Gender gender,

        @NotBlank(message = "Phone is required")
        @Pattern(regexp = "^\\d{10}$", message = "Phone must be exactly 10 digits")
        String phone,

        @Size(max = 300, message = "Address must be at most 300 characters")
        String address
) {
}