package com.darshan.hospital.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Duration;
import java.time.Instant;

@Entity
@Table(name = "appointments")
public class Appointment {

    public static final Duration SLOT = Duration.ofMinutes(30);

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @Column(name = "start_time", nullable = false)
    private Instant startTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AppointmentStatus status = AppointmentStatus.BOOKED;

    @Column(length = 500)
    private String reason;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected Appointment() {
        // required by JPA
    }

    public Appointment(Doctor doctor, Patient patient, Instant startTime, String reason) {
        this.doctor = doctor;
        this.patient = patient;
        this.startTime = startTime;
        this.reason = reason;
    }

    @PrePersist
    void onCreate() {
        this.createdAt = Instant.now();
    }

    public void markCancelled() { this.status = AppointmentStatus.CANCELLED; }
    public void markCompleted() { this.status = AppointmentStatus.COMPLETED; }

    public Instant getEndTime() { return startTime.plus(SLOT); }

    public Long getId() { return id; }
    public Doctor getDoctor() { return doctor; }
    public Patient getPatient() { return patient; }
    public Instant getStartTime() { return startTime; }
    public AppointmentStatus getStatus() { return status; }
    public String getReason() { return reason; }
    public Instant getCreatedAt() { return createdAt; }
}