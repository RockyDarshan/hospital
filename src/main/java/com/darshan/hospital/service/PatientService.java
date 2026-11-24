package com.darshan.hospital.service;

import com.darshan.hospital.dto.PageResponse;
import com.darshan.hospital.dto.PatientResponse;
import com.darshan.hospital.dto.RegisterPatientRequest;
import com.darshan.hospital.dto.UpdatePatientRequest;
import com.darshan.hospital.entity.Patient;
import com.darshan.hospital.entity.Role;
import com.darshan.hospital.entity.User;
import com.darshan.hospital.exception.ResourceNotFoundException;
import com.darshan.hospital.repository.PatientRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PatientService {

    private static final int MAX_PAGE_SIZE = 50;

    private final PatientRepository patients;
    private final UserAccountService accounts;

    public PatientService(PatientRepository patients, UserAccountService accounts) {
        this.patients = patients;
        this.accounts = accounts;
    }

    @Transactional
    public PatientResponse register(RegisterPatientRequest request) {
        User user = accounts.createAccount(request.email(), request.password(), Role.PATIENT);

        Patient patient = new Patient(
                user, request.fullName().trim(), request.dateOfBirth(),
                request.gender(), request.phone(), request.address());
        return PatientResponse.from(patients.save(patient));
    }

    @Transactional(readOnly = true)
    public PageResponse<PatientResponse> search(String name, int page, int size) {
        Pageable pageable = PageRequest.of(
                Math.max(page, 0),
                Math.clamp(size, 1, MAX_PAGE_SIZE),
                Sort.by("fullName").ascending().and(Sort.by("id")));
        String term = name == null ? "" : name.trim();

        Page<Patient> result = patients.findByFullNameContainingIgnoreCase(term, pageable);
        return PageResponse.from(result.map(PatientResponse::from));
    }

    @Transactional(readOnly = true)
    public PatientResponse findById(Long id) {
        return PatientResponse.from(getOrThrow(id));
    }

    @Transactional
    public PatientResponse update(Long id, UpdatePatientRequest request) {
        Patient patient = getOrThrow(id);
        patient.setFullName(request.fullName().trim());
        patient.setDateOfBirth(request.dateOfBirth());
        patient.setGender(request.gender());
        patient.setPhone(request.phone());
        patient.setAddress(request.address());
        return PatientResponse.from(patient);
    }

    private Patient getOrThrow(Long id) {
        return patients.findWithUserById(id).orElseThrow(
                () -> new ResourceNotFoundException("Patient not found with id " + id));
    }
}