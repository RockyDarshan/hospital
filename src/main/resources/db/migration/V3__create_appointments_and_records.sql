CREATE TABLE appointments (
                              id         BIGSERIAL PRIMARY KEY,
                              doctor_id  BIGINT      NOT NULL REFERENCES doctors (id),
                              patient_id BIGINT      NOT NULL REFERENCES patients (id),
                              start_time TIMESTAMPTZ NOT NULL,
                              status     VARCHAR(20) NOT NULL
                                  CHECK (status IN ('BOOKED', 'CANCELLED', 'COMPLETED')),
                              reason     VARCHAR(500),
                              created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- One active appointment per doctor per slot, and per patient per slot.
-- Cancelled appointments are ignored, so a freed slot can be booked again.
CREATE UNIQUE INDEX uq_appointments_doctor_slot
    ON appointments (doctor_id, start_time) WHERE status <> 'CANCELLED';
CREATE UNIQUE INDEX uq_appointments_patient_slot
    ON appointments (patient_id, start_time) WHERE status <> 'CANCELLED';

CREATE INDEX idx_appointments_doctor ON appointments (doctor_id, start_time);
CREATE INDEX idx_appointments_patient ON appointments (patient_id, start_time);

CREATE TABLE medical_records (
                                 id             BIGSERIAL PRIMARY KEY,
                                 appointment_id BIGINT        NOT NULL UNIQUE REFERENCES appointments (id),
                                 diagnosis      VARCHAR(2000) NOT NULL,
                                 prescription   VARCHAR(2000),
                                 notes          VARCHAR(4000),
                                 created_at     TIMESTAMPTZ   NOT NULL DEFAULT now()
);