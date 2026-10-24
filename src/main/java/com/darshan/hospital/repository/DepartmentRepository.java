package com.darshan.hospital.repository;

import com.darshan.hospital.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DepartmentRepository extends JpaRepository<Department,Long> {

    boolean existsByNameIgnoreCase(String name);
}
