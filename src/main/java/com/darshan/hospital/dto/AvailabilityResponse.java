package com.darshan.hospital.dto;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

public record AvailabilityResponse(Long doctorId, LocalDate date, List<OffsetDateTime> freeSlots) {
}