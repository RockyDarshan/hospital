package com.darshan.hospital.repository;

import com.darshan.hospital.entity.Appointment;
import com.darshan.hospital.entity.AppointmentStatus;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    boolean existsByDoctorIdAndStartTimeAndStatusNot(
            Long doctorId, Instant startTime, AppointmentStatus status);

    boolean existsByPatientIdAndStartTimeAndStatusNot(
            Long patientId, Instant startTime, AppointmentStatus status);

    @EntityGraph(attributePaths = {
            "doctor", "doctor.user", "doctor.department", "patient", "patient.user"})
    Optional<Appointment> findWithDetailsById(Long id);

    @EntityGraph(attributePaths = {"doctor", "doctor.department", "patient"})
    Page<Appointment> findByPatientId(Long patientId, Pageable pageable);

    @EntityGraph(attributePaths = {"doctor", "doctor.department", "patient"})
    Page<Appointment> findByPatientIdAndStatus(
            Long patientId, AppointmentStatus status, Pageable pageable);

    @EntityGraph(attributePaths = {"doctor", "doctor.department", "patient"})
    Page<Appointment> findByDoctorId(Long doctorId, Pageable pageable);

    @EntityGraph(attributePaths = {"doctor", "doctor.department", "patient"})
    Page<Appointment> findByDoctorIdAndStatus(
            Long doctorId, AppointmentStatus status, Pageable pageable);

    @Query("select a.startTime from Appointment a "
            + "where a.doctor.id = :doctorId and a.status <> :excluded "
            + "and a.startTime >= :rangeStart and a.startTime < :rangeEnd")
    List<Instant> findTakenSlots(@Param("doctorId") Long doctorId,
                                 @Param("excluded") AppointmentStatus excluded,
                                 @Param("rangeStart") Instant rangeStart,
                                 @Param("rangeEnd") Instant rangeEnd);
}