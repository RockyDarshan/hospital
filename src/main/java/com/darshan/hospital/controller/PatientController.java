package com.darshan.hospital.controller;

import com.darshan.hospital.dto.PageResponse;
import com.darshan.hospital.dto.PatientResponse;
import com.darshan.hospital.dto.UpdatePatientRequest;
import com.darshan.hospital.service.PatientService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/patients")
public class PatientController {

    private final PatientService service;

    public PatientController(PatientService service) {
        this.service = service;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public PageResponse<PatientResponse> search(
            @RequestParam(required = false) String name,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return service.search(name, page, size);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @access.isPatientOwner(#id, authentication)")
    public PatientResponse getById(@PathVariable Long id) {
        return service.findById(id);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @access.isPatientOwner(#id, authentication)")
    public PatientResponse update(
            @PathVariable Long id, @Valid @RequestBody UpdatePatientRequest request) {
        return service.update(id, request);
    }
}