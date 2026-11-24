package com.darshan.hospital.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "doctors")
public class Doctor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    @Column(name = "full_name", nullable = false, length = 150)
    private String fullName;

    @Column(nullable = false, length = 20)
    private String phone;

    @Column(length = 200)
    private String qualification;

    protected Doctor() {
        // required by JPA
    }

    public Doctor(User user, Department department, String fullName, String phone, String qualification) {
        this.user = user;
        this.department = department;
        this.fullName = fullName;
        this.phone = phone;
        this.qualification = qualification;
    }

    public Long getId() { return id; }
    public User getUser() { return user; }
    public Department getDepartment() { return department; }
    public void setDepartment(Department department) { this.department = department; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getQualification() { return qualification; }
    public void setQualification(String qualification) { this.qualification = qualification; }
}