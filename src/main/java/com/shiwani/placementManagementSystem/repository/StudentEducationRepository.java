package com.shiwani.placementManagementSystem.repository;

import com.shiwani.placementManagementSystem.entity.StudentEducation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StudentEducationRepository extends JpaRepository<StudentEducation, Long> {

    List<StudentEducation> findByStudentId(Long studentId);
}