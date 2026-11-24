CREATE TABLE users (
                       id            BIGSERIAL PRIMARY KEY,
                       email         VARCHAR(255) NOT NULL UNIQUE,
                       password_hash VARCHAR(100) NOT NULL,
                       role          VARCHAR(20)  NOT NULL CHECK (role IN ('ADMIN', 'DOCTOR', 'PATIENT')),
                       enabled       BOOLEAN      NOT NULL DEFAULT TRUE,
                       created_at    TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE TABLE doctors (
                         id            BIGSERIAL PRIMARY KEY,
                         user_id       BIGINT       NOT NULL UNIQUE REFERENCES users (id),
                         department_id BIGINT       NOT NULL REFERENCES departments (id),
                         full_name     VARCHAR(150) NOT NULL,
                         phone         VARCHAR(20)  NOT NULL,
                         qualification VARCHAR(200)
);

CREATE INDEX idx_doctors_department_id ON doctors (department_id);

CREATE TABLE patients (
                          id            BIGSERIAL PRIMARY KEY,
                          user_id       BIGINT       NOT NULL UNIQUE REFERENCES users (id),
                          full_name     VARCHAR(150) NOT NULL,
                          date_of_birth DATE         NOT NULL,
                          gender        VARCHAR(10)  NOT NULL CHECK (gender IN ('MALE', 'FEMALE', 'OTHER')),
                          phone         VARCHAR(20)  NOT NULL,
                          address       VARCHAR(300)
);