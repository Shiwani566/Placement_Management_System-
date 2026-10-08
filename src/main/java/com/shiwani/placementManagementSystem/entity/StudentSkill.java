package com.shiwani.placementManagementSystem.entity;

import jakarta.persistence.*;

@Entity
@Table(
        name = "student_skills",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"student_id", "skill_id"})
        }
)
public class StudentSkill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne
    @JoinColumn(name = "skill_id", nullable = false)
    private Skill skill;

    public StudentSkill() {
    }

    public StudentSkill(Student student, Skill skill) {
        this.student = student;
        this.skill = skill;
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

    public Skill getSkill() {
        return skill;
    }

    public void setSkill(Skill skill) {
        this.skill = skill;
    }
}