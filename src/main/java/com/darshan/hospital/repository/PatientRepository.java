package com.darshan.hospital.repository;

import com.darshan.hospital.entity.Patient;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PatientRepository extends JpaRepository<Patient, Long> {

    @EntityGraph(attributePaths = "user")
    Page<Patient> findByFullNameContainingIgnoreCase(String name, Pageable pageable);

    boolean existsByIdAndUserId(Long id, Long userId);

    @EntityGraph(attributePaths = "user")
    Optional<Patient> findWithUserById(Long id);
}