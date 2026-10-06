package com.shiwani.placementManagementSystem.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "student_education")
public class StudentEducation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @Column(nullable = false)
    private String qualification;

    @Column(nullable = false)
    private String institution;

    private String boardOrUniversity;

    private Integer passingYear;

    private Double percentage;

    public StudentEducation() {
    }

    public StudentEducation(Student student, String qualification,
                            String institution, String boardOrUniversity,
                            Integer passingYear, Double percentage) {
        this.student = student;
        this.qualification = qualification;
        this.institution = institution;
        this.boardOrUniversity = boardOrUniversity;
        this.passingYear = passingYear;
        this.percentage = percentage;
    }

    public Long getId() {
        return id;
    }

    public Student getStudent() {
        return student;
    }

    public void setStudent(Student student) {
        this.student = student;
    }

    public String getQualification() {
        return qualification;
    }

    public void setQualification(String qualification) {
        this.qualification = qualification;
    }

    public String getInstitution() {
        return institution;
    }

    public void setInstitution(String institution) {
        this.institution = institution;
    }

    public String getBoardOrUniversity() {
        return boardOrUniversity;
    }

    public void setBoardOrUniversity(String boardOrUniversity) {
        this.boardOrUniversity = boardOrUniversity;
    }

    public Integer getPassingYear() {
        return passingYear;
    }

    public void setPassingYear(Integer passingYear) {
        this.passingYear = passingYear;
    }

    public Double getPercentage() {
        return percentage;
    }

    public void setPercentage(Double percentage) {
        this.percentage = percentage;
    }
}