package com.shiwani.placementManagementSystem.repository;

import com.shiwani.placementManagementSystem.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student, Long> {

    Optional<Student> findByUserId(Long userId);
}