package com.darshan.hospital.service;

import com.darshan.hospital.dto.DepartmentRequest;
import com.darshan.hospital.dto.DepartmentResponse;
import com.darshan.hospital.entity.Department;
import com.darshan.hospital.exception.DuplicateResourceException;
import com.darshan.hospital.exception.ResourceNotFoundException;
import com.darshan.hospital.repository.DepartmentRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DepartmentService {

    private final DepartmentRepository repository;

    public DepartmentService(DepartmentRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<DepartmentResponse> findAll() {
        return repository.findAll().stream().map(DepartmentResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public DepartmentResponse findById(Long id) {
        return DepartmentResponse.from(getOrThrow(id));
    }

    @Transactional
    public DepartmentResponse create(DepartmentRequest request) {
        String name = request.name().trim();
        if (repository.existsByNameIgnoreCase(name)) {
            throw new DuplicateResourceException("Department already exists: " + name);
        }
        Department saved = repository.save(new Department(name, request.description()));
        return DepartmentResponse.from(saved);
    }

    @Transactional
    public DepartmentResponse update(Long id, DepartmentRequest request) {
        Department department = getOrThrow(id);
        String name = request.name().trim();

        boolean nameChanged = !department.getName().equalsIgnoreCase(name);
        if (nameChanged && repository.existsByNameIgnoreCase(name)) {
            throw new DuplicateResourceException("Department already exists: " + name);
        }

        department.setName(name);
        department.setDescription(request.description());
        return DepartmentResponse.from(department);
    }

    @Transactional
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Department not found with id " + id);
        }
        repository.deleteById(id);
    }

    private Department getOrThrow(Long id) {
        return repository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Department not found with id " + id));
    }
}