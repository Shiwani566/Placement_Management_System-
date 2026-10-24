package com.shiwani.placementManagementSystem.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "placement_applications",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_student_placement_drive",
                        columnNames = {"student_id", "placement_drive_id"}
                )
        }
)
public class PlacementApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    @JsonIgnoreProperties({"user", "hibernateLazyInitializer", "handler"})
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "placement_drive_id", nullable = false)
    private PlacementDrive placementDrive;

    @Column(nullable = false, length = 30)
    private String status = "APPLIED";

    @Column(nullable = false, updatable = false)
    private LocalDateTime appliedAt;

    @PrePersist
    protected void onCreate() {
        if (appliedAt == null) {
            appliedAt = LocalDateTime.now();
        }
    }

    public PlacementApplication() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Student getStudent() {
        return student;
    }

    public void setStudent(Student student) {
        this.student = student;
    }

    public PlacementDrive getPlacementDrive() {
        return placementDrive;
    }

    public void setPlacementDrive(PlacementDrive placementDrive) {
        this.placementDrive = placementDrive;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getAppliedAt() {
        return appliedAt;
    }

    public void setAppliedAt(LocalDateTime appliedAt) {
        this.appliedAt = appliedAt;
    }
}

