package com.darshan.hospital.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "medical_records")
public class MedicalRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "appointment_id", nullable = false, unique = true)
    private Appointment appointment;

    @Column(nullable = false, length = 2000)
    private String diagnosis;

    @Column(length = 2000)
    private String prescription;

    @Column(length = 4000)
    private String notes;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected MedicalRecord() {
        // required by JPA
    }

    public MedicalRecord(Appointment appointment, String diagnosis,
                         String prescription, String notes) {
        this.appointment = appointment;
        this.diagnosis = diagnosis;
        this.prescription = prescription;
        this.notes = notes;
    }

    @PrePersist
    void onCreate() {
        this.createdAt = Instant.now();
    }

    public Long getId() { return id; }
    public Appointment getAppointment() { return appointment; }
    public String getDiagnosis() { return diagnosis; }
    public String getPrescription() { return prescription; }
    public String getNotes() { return notes; }
    public Instant getCreatedAt() { return createdAt; }
}