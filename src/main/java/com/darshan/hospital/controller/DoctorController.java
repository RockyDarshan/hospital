package com.darshan.hospital.controller;

import com.darshan.hospital.dto.CreateDoctorRequest;
import com.darshan.hospital.dto.DoctorResponse;
import com.darshan.hospital.dto.PageResponse;
import com.darshan.hospital.dto.UpdateDoctorRequest;
import com.darshan.hospital.service.DoctorService;
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
@RequestMapping("/api/doctors")
public class DoctorController {

    private final DoctorService service;

    public DoctorController(DoctorService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<DoctorResponse> create(@Valid @RequestBody CreateDoctorRequest request) {
        DoctorResponse created = service.create(request);
        return ResponseEntity.created(URI.create("/api/doctors/" + created.id())).body(created);
    }

    @GetMapping
    public PageResponse<DoctorResponse> search(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return service.search(name, departmentId, page, size);
    }

    @GetMapping("/{id}")
    public DoctorResponse getById(@PathVariable Long id) {
        return service.findById(id);
    }

    @PutMapping("/{id}")
    public DoctorResponse update(
            @PathVariable Long id, @Valid @RequestBody UpdateDoctorRequest request) {
        return service.update(id, request);
    }
}