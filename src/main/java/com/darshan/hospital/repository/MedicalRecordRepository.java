package com.darshan.hospital.repository;

import com.darshan.hospital.entity.MedicalRecord;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MedicalRecordRepository extends JpaRepository<MedicalRecord, Long> {

    boolean existsByAppointmentId(Long appointmentId);

    Optional<MedicalRecord> findByAppointmentId(Long appointmentId);
}