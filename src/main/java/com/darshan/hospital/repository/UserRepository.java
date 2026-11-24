package com.darshan.hospital.repository;

import com.darshan.hospital.entity.Role;
import com.darshan.hospital.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User,Long> {

    boolean existsByEmail(String email);

    boolean existsByRole(Role role);

    Optional<User> findByEmail(String email);
}
