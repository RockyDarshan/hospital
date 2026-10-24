package com.darshan.hospital.repository;

import com.darshan.hospital.entity.Doctor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DoctorRepository  extends JpaRepository<Doctor,Long> {

    @EntityGraph(attributePaths = {"department","user"})
    Page<Doctor> findByFullNameContainingIgnoreCase(String name, Pageable pageable);


    @EntityGraph(attributePaths = {"department", "user"})
    Page<Doctor> findByDepartmentIdAndFullNameContainingIgnoreCase(
            Long departmentId, String name, Pageable pageable);

    @EntityGraph(attributePaths = {"department", "user"})
    Optional<Doctor> findWithDetailsById(Long id);
}
