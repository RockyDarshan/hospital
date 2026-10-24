package com.darshan.hospital.controller;

import com.darshan.hospital.dto.PageResponse;
import com.darshan.hospital.dto.PatientResponse;
import com.darshan.hospital.dto.RegisterPatientRequest;
import com.darshan.hospital.dto.UpdatePatientRequest;
import com.darshan.hospital.service.PatientService;
import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
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

    @PostMapping
    public ResponseEntity<PatientResponse> register(
            @Valid @RequestBody RegisterPatientRequest request) {
        PatientResponse created = service.register(request);
        return ResponseEntity.created(URI.create("/api/patients/" + created.id())).body(created);
    }

    @GetMapping
    public PageResponse<PatientResponse> search(
            @RequestParam(required = false) String name,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return service.search(name, page, size);
    }

    @GetMapping("/{id}")
    public PatientResponse getById(@PathVariable Long id) {
        return service.findById(id);
    }

    @PutMapping("/{id}")
    public PatientResponse update(
            @PathVariable Long id, @Valid @RequestBody UpdatePatientRequest request) {
        return service.update(id, request);
    }
}