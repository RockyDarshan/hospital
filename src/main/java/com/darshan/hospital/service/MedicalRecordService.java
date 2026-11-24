package com.darshan.hospital.service;

import com.darshan.hospital.dto.MedicalRecordRequest;
import com.darshan.hospital.dto.MedicalRecordResponse;
import com.darshan.hospital.entity.Appointment;
import com.darshan.hospital.entity.AppointmentStatus;
import com.darshan.hospital.entity.MedicalRecord;
import com.darshan.hospital.exception.ConflictException;
import com.darshan.hospital.exception.ResourceNotFoundException;
import com.darshan.hospital.repository.AppointmentRepository;
import com.darshan.hospital.repository.MedicalRecordRepository;
import com.darshan.hospital.security.CurrentUser;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MedicalRecordService {

    private final AppointmentRepository appointments;
    private final MedicalRecordRepository records;

    public MedicalRecordService(AppointmentRepository appointments,
                                MedicalRecordRepository records) {
        this.appointments = appointments;
        this.records = records;
    }

    @Transactional
    public MedicalRecordResponse create(
            CurrentUser user, Long appointmentId, MedicalRecordRequest request) {
        Appointment appointment = getWithDetails(appointmentId);

        if (!appointment.getDoctor().getUser().getId().equals(user.id())) {
            throw new AccessDeniedException("Only the treating doctor can add a record.");
        }
        if (appointment.getStatus() != AppointmentStatus.COMPLETED) {
            throw new ConflictException(
                    "A record can be added only after the appointment is completed.");
        }
        if (records.existsByAppointmentId(appointmentId)) {
            throw new ConflictException("A record already exists for this appointment.");
        }

        MedicalRecord record = new MedicalRecord(
                appointment, request.diagnosis().trim(),
                request.prescription(), request.notes());
        try {
            records.saveAndFlush(record);
        } catch (DataIntegrityViolationException e) {
            throw new ConflictException("A record already exists for this appointment.");
        }
        return MedicalRecordResponse.from(record, appointmentId);
    }

    // Only the treating doctor and the patient can read it. Admins cannot.
    @Transactional(readOnly = true)
    public MedicalRecordResponse read(CurrentUser user, Long appointmentId) {
        Appointment appointment = getWithDetails(appointmentId);

        boolean isPatient = appointment.getPatient().getUser().getId().equals(user.id());
        boolean isDoctor = appointment.getDoctor().getUser().getId().equals(user.id());
        if (!isPatient && !isDoctor) {
            throw new AccessDeniedException("You cannot view this record.");
        }

        MedicalRecord record = records.findByAppointmentId(appointmentId).orElseThrow(
                () -> new ResourceNotFoundException("No record for this appointment yet."));
        return MedicalRecordResponse.from(record, appointmentId);
    }

    private Appointment getWithDetails(Long id) {
        return appointments.findWithDetailsById(id).orElseThrow(
                () -> new ResourceNotFoundException("Appointment not found with id " + id));
    }
}