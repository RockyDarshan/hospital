package com.darshan.hospital.controller;

import com.darshan.hospital.dto.AppointmentResponse;
import com.darshan.hospital.dto.AvailabilityResponse;
import com.darshan.hospital.dto.BookAppointmentRequest;
import com.darshan.hospital.dto.MedicalRecordRequest;
import com.darshan.hospital.dto.MedicalRecordResponse;
import com.darshan.hospital.dto.PageResponse;
import com.darshan.hospital.entity.AppointmentStatus;
import com.darshan.hospital.security.CurrentUser;
import com.darshan.hospital.service.AppointmentService;
import com.darshan.hospital.service.MedicalRecordService;
import jakarta.validation.Valid;
import java.net.URI;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {

    private final AppointmentService appointments;
    private final MedicalRecordService records;

    public AppointmentController(AppointmentService appointments, MedicalRecordService records) {
        this.appointments = appointments;
        this.records = records;
    }

    @PostMapping
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<AppointmentResponse> book(
            @AuthenticationPrincipal Jwt jwt, @Valid @RequestBody BookAppointmentRequest request) {
        AppointmentResponse created = appointments.book(CurrentUser.from(jwt), request);
        return ResponseEntity.created(URI.create("/api/appointments/" + created.id()))
                .body(created);
    }

    @GetMapping("/availability")
    public AvailabilityResponse availability(
            @RequestParam Long doctorId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return appointments.availability(doctorId, date);
    }

    @GetMapping("/mine")
    @PreAuthorize("hasAnyRole('PATIENT', 'DOCTOR')")
    public PageResponse<AppointmentResponse> mine(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(required = false) AppointmentStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return appointments.mine(CurrentUser.from(jwt), status, page, size);
    }

    @GetMapping("/{id}")
    public AppointmentResponse getById(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        return appointments.getById(CurrentUser.from(jwt), id);
    }

    @PatchMapping("/{id}/cancel")
    public AppointmentResponse cancel(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        return appointments.cancel(CurrentUser.from(jwt), id);
    }

    @PatchMapping("/{id}/complete")
    @PreAuthorize("hasRole('DOCTOR')")
    public AppointmentResponse complete(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        return appointments.complete(CurrentUser.from(jwt), id);
    }

    @PostMapping("/{id}/record")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<MedicalRecordResponse> addRecord(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long id,
            @Valid @RequestBody MedicalRecordRequest request) {
        MedicalRecordResponse created = records.create(CurrentUser.from(jwt), id, request);
        return ResponseEntity.created(URI.create("/api/appointments/" + id + "/record"))
                .body(created);
    }

    @GetMapping("/{id}/record")
    @PreAuthorize("hasAnyRole('PATIENT', 'DOCTOR')")
    public MedicalRecordResponse getRecord(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        return records.read(CurrentUser.from(jwt), id);
    }
}