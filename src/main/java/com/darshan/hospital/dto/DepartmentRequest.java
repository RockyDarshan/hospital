package com.darshan.hospital.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DepartmentRequest(
       @NotBlank(message = "Name is required")
       @Size(max = 100,message = "Name must be most 100 characters")
        String name,

        @Size(max = 500,message = "Description must be at most 500 characters")
        String description
) {
}
