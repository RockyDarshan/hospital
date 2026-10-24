package com.darshan.hospital.service;

import com.darshan.hospital.dto.CreateDoctorRequest;
import com.darshan.hospital.dto.DoctorResponse;
import com.darshan.hospital.dto.PageResponse;
import com.darshan.hospital.dto.UpdateDoctorRequest;
import com.darshan.hospital.entity.Department;
import com.darshan.hospital.entity.Doctor;
import com.darshan.hospital.entity.Role;
import com.darshan.hospital.entity.User;
import com.darshan.hospital.exception.ResourceNotFoundException;
import com.darshan.hospital.repository.DepartmentRepository;
import com.darshan.hospital.repository.DoctorRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DoctorService {

    private static final int MAX_PAGE_SIZE = 50;

    private final DoctorRepository doctors;
    private final DepartmentRepository departments;
    private final UserAccountService accounts;

    public DoctorService(DoctorRepository doctors,
                         DepartmentRepository departments,
                         UserAccountService accounts) {
        this.doctors = doctors;
        this.departments = departments;
        this.accounts = accounts;
    }

    @Transactional
    public DoctorResponse create(CreateDoctorRequest request) {
        Department department = getDepartment(request.departmentId());
        User user = accounts.createAccount(request.email(), request.password(), Role.DOCTOR);

        Doctor doctor = new Doctor(
                user, department, request.fullName().trim(),
                request.phone(), request.qualification());
        return DoctorResponse.from(doctors.save(doctor));
    }

    @Transactional(readOnly = true)
    public PageResponse<DoctorResponse> search(String name, Long departmentId, int page, int size) {
        Pageable pageable = PageRequest.of(
                Math.max(page, 0),
                Math.min(Math.max(size, 1), MAX_PAGE_SIZE),
                Sort.by("fullName").ascending().and(Sort.by("id")));
        String term = name == null ? "" : name.trim();

        Page<Doctor> result = departmentId == null
                ? doctors.findByFullNameContainingIgnoreCase(term, pageable)
                : doctors.findByDepartmentIdAndFullNameContainingIgnoreCase(
                departmentId, term, pageable);

        return PageResponse.from(result.map(DoctorResponse::from));
    }

    @Transactional(readOnly = true)
    public DoctorResponse findById(Long id) {
        return DoctorResponse.from(getOrThrow(id));
    }

    @Transactional
    public DoctorResponse update(Long id, UpdateDoctorRequest request) {
        Doctor doctor = getOrThrow(id);
        doctor.setDepartment(getDepartment(request.departmentId()));
        doctor.setFullName(request.fullName().trim());
        doctor.setPhone(request.phone());
        doctor.setQualification(request.qualification());
        return DoctorResponse.from(doctor);
    }

    private Doctor getOrThrow(Long id) {
        return doctors.findWithDetailsById(id).orElseThrow(
                () -> new ResourceNotFoundException("Doctor not found with id " + id));
    }

    private Department getDepartment(Long id) {
        return departments.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Department not found with id " + id));
    }
}