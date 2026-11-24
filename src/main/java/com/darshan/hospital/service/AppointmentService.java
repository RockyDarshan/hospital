package com.darshan.hospital.service;

import com.darshan.hospital.dto.AppointmentResponse;
import com.darshan.hospital.dto.AvailabilityResponse;
import com.darshan.hospital.dto.BookAppointmentRequest;
import com.darshan.hospital.dto.PageResponse;
import com.darshan.hospital.entity.Appointment;
import com.darshan.hospital.entity.AppointmentStatus;
import com.darshan.hospital.entity.Doctor;
import com.darshan.hospital.entity.Patient;
import com.darshan.hospital.entity.Role;
import com.darshan.hospital.exception.BadRequestException;
import com.darshan.hospital.exception.ConflictException;
import com.darshan.hospital.exception.ResourceNotFoundException;
import com.darshan.hospital.repository.AppointmentRepository;
import com.darshan.hospital.repository.DoctorRepository;
import com.darshan.hospital.repository.PatientRepository;
import com.darshan.hospital.security.CurrentUser;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AppointmentService {

    private static final LocalTime OPENING = LocalTime.of(9, 0);
    private static final LocalTime CLOSING = LocalTime.of(17, 0); // last slot starts 16:30
    private static final int MAX_DAYS_AHEAD = 90;
    private static final int MAX_PAGE_SIZE = 50;

    private final AppointmentRepository appointments;
    private final DoctorRepository doctors;
    private final PatientRepository patients;
    private final ZoneId clinicZone;

    public AppointmentService(AppointmentRepository appointments,
                              DoctorRepository doctors,
                              PatientRepository patients,
                              @Value("${app.clinic.zone:Asia/Kolkata}") String zone) {
        this.appointments = appointments;
        this.doctors = doctors;
        this.patients = patients;
        this.clinicZone = ZoneId.of(zone);
    }

    @Transactional
    public AppointmentResponse book(CurrentUser user, BookAppointmentRequest request) {
        Patient patient = patients.findByUserId(user.id()).orElseThrow(
                () -> new AccessDeniedException("Only patients can book appointments."));
        Doctor doctor = doctors.findWithDetailsById(request.doctorId()).orElseThrow(
                () -> new ResourceNotFoundException(
                        "Doctor not found with id " + request.doctorId()));

        Instant start = request.startTime().toInstant();
        validateSlot(start);

        if (appointments.existsByDoctorIdAndStartTimeAndStatusNot(
                doctor.getId(), start, AppointmentStatus.CANCELLED)) {
            throw new ConflictException("The doctor is already booked at this time.");
        }
        if (appointments.existsByPatientIdAndStartTimeAndStatusNot(
                patient.getId(), start, AppointmentStatus.CANCELLED)) {
            throw new ConflictException("You already have an appointment at this time.");
        }

        Appointment appointment =
                new Appointment(doctor, patient, start, request.reason());
        try {
            // Flush now so a constraint violation surfaces here, not at commit time.
            appointments.saveAndFlush(appointment);
        } catch (DataIntegrityViolationException e) {
            // Two requests raced for the same slot; the database picked the winner.
            throw new ConflictException("That time slot is no longer available.");
        }
        return AppointmentResponse.from(appointment, clinicZone);
    }

    @Transactional(readOnly = true)
    public PageResponse<AppointmentResponse> mine(
            CurrentUser user, AppointmentStatus status, int page, int size) {
        Pageable pageable = PageRequest.of(
                Math.max(page, 0),
                Math.min(Math.max(size, 1), MAX_PAGE_SIZE),
                Sort.by(Sort.Direction.DESC, "startTime").and(Sort.by("id")));

        Page<Appointment> result;
        if (user.role() == Role.PATIENT) {
            Patient patient = patients.findByUserId(user.id()).orElseThrow(
                    () -> new AccessDeniedException("No patient profile for this account."));
            result = status == null
                    ? appointments.findByPatientId(patient.getId(), pageable)
                    : appointments.findByPatientIdAndStatus(patient.getId(), status, pageable);
        } else {
            Doctor doctor = doctors.findByUserId(user.id()).orElseThrow(
                    () -> new AccessDeniedException("No doctor profile for this account."));
            result = status == null
                    ? appointments.findByDoctorId(doctor.getId(), pageable)
                    : appointments.findByDoctorIdAndStatus(doctor.getId(), status, pageable);
        }
        return PageResponse.from(result.map(a -> AppointmentResponse.from(a, clinicZone)));
    }

    @Transactional(readOnly = true)
    public AppointmentResponse getById(CurrentUser user, Long id) {
        Appointment appointment = getWithDetails(id);
        if (user.role() != Role.ADMIN
                && !isPatientOwner(appointment, user)
                && !isDoctorOwner(appointment, user)) {
            throw new AccessDeniedException("You cannot view this appointment.");
        }
        return AppointmentResponse.from(appointment, clinicZone);
    }

    @Transactional
    public AppointmentResponse cancel(CurrentUser user, Long id) {
        Appointment appointment = getWithDetails(id);
        if (user.role() != Role.ADMIN
                && !isPatientOwner(appointment, user)
                && !isDoctorOwner(appointment, user)) {
            throw new AccessDeniedException("You cannot cancel this appointment.");
        }
        if (appointment.getStatus() != AppointmentStatus.BOOKED) {
            throw new ConflictException("Only booked appointments can be cancelled.");
        }
        if (!appointment.getStartTime().isAfter(Instant.now())) {
            throw new ConflictException("An appointment that has started cannot be cancelled.");
        }
        appointment.markCancelled();
        return AppointmentResponse.from(appointment, clinicZone);
    }

    @Transactional
    public AppointmentResponse complete(CurrentUser user, Long id) {
        Appointment appointment = getWithDetails(id);
        if (!isDoctorOwner(appointment, user)) {
            throw new AccessDeniedException("Only the treating doctor can complete this.");
        }
        if (appointment.getStatus() != AppointmentStatus.BOOKED) {
            throw new ConflictException("Only booked appointments can be completed.");
        }
        if (appointment.getStartTime().isAfter(Instant.now())) {
            throw new ConflictException("An appointment cannot be completed before it starts.");
        }
        appointment.markCompleted();
        return AppointmentResponse.from(appointment, clinicZone);
    }

    @Transactional(readOnly = true)
    public AvailabilityResponse availability(Long doctorId, LocalDate date) {
        if (!doctors.existsById(doctorId)) {
            throw new ResourceNotFoundException("Doctor not found with id " + doctorId);
        }
        ZonedDateTime open = date.atTime(OPENING).atZone(clinicZone);
        ZonedDateTime close = date.atTime(CLOSING).atZone(clinicZone);

        Set<Instant> taken = new HashSet<>(appointments.findTakenSlots(
                doctorId, AppointmentStatus.CANCELLED, open.toInstant(), close.toInstant()));

        Instant now = Instant.now();
        Instant limit = now.plus(Duration.ofDays(MAX_DAYS_AHEAD));
        List<OffsetDateTime> free = new ArrayList<>();
        for (ZonedDateTime slot = open; slot.isBefore(close); slot = slot.plus(Appointment.SLOT)) {
            Instant at = slot.toInstant();
            if (at.isAfter(now) && !at.isAfter(limit) && !taken.contains(at)) {
                free.add(slot.toOffsetDateTime());
            }
        }
        return new AvailabilityResponse(doctorId, date, free);
    }

    private void validateSlot(Instant start) {
        Instant now = Instant.now();
        if (!start.isAfter(now)) {
            throw new BadRequestException("Appointment time must be in the future.");
        }
        if (start.isAfter(now.plus(Duration.ofDays(MAX_DAYS_AHEAD)))) {
            throw new BadRequestException(
                    "Appointments can be booked at most " + MAX_DAYS_AHEAD + " days ahead.");
        }

        ZonedDateTime local = start.atZone(clinicZone);
        boolean onGrid = local.getSecond() == 0 && local.getNano() == 0
                && (local.getMinute() == 0 || local.getMinute() == 30);
        if (!onGrid) {
            throw new BadRequestException("Appointments start on the hour or the half hour.");
        }
        LocalTime time = local.toLocalTime();
        if (time.isBefore(OPENING) || !time.isBefore(CLOSING)) {
            throw new BadRequestException(
                    "The clinic is open 09:00 to 17:00. The last slot starts at 16:30.");
        }
    }

    private Appointment getWithDetails(Long id) {
        return appointments.findWithDetailsById(id).orElseThrow(
                () -> new ResourceNotFoundException("Appointment not found with id " + id));
    }

    private boolean isPatientOwner(Appointment a, CurrentUser user) {
        return a.getPatient().getUser().getId().equals(user.id());
    }

    private boolean isDoctorOwner(Appointment a, CurrentUser user) {
        return a.getDoctor().getUser().getId().equals(user.id());
    }
}