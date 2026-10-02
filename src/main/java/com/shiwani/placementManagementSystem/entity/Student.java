package com.shiwani.placementManagementSystem.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "students")
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(nullable = false)
    private String name;

    private String phone;

    @Column(nullable = false)
    private String branch;

    @Column(nullable = false)
    private Integer graduationYear;

    @Column(nullable = false)
    private Double cgpa;

    @Column(nullable = false)
    private Integer backlogs = 0;

    public Student() {
    }

    public Student(User user, String name, String phone,
                   String branch, Integer graduationYear,
                   Double cgpa, Integer backlogs) {
        this.user = user;
        this.name = name;
        this.phone = phone;
        this.branch = branch;
        this.graduationYear = graduationYear;
        this.cgpa = cgpa;
        this.backlogs = backlogs;
    }

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }

    public Integer getGraduationYear() {
        return graduationYear;
    }

    public void setGraduationYear(Integer graduationYear) {
        this.graduationYear = graduationYear;
    }

    public Double getCgpa() {
        return cgpa;
    }

    public void setCgpa(Double cgpa) {
        this.cgpa = cgpa;
    }

    public Integer getBacklogs() {
        return backlogs;
    }

    public void setBacklogs(Integer backlogs) {
        this.backlogs = backlogs;
    }
}