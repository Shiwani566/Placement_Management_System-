package com.shiwani.placementManagementSystem.service;

import com.shiwani.placementManagementSystem.entity.StudentEducation;
import com.shiwani.placementManagementSystem.repository.StudentEducationRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class StudentEducationService {

    private final StudentEducationRepository studentEducationRepository;

    public StudentEducationService(StudentEducationRepository studentEducationRepository) {
        this.studentEducationRepository = studentEducationRepository;
    }

    public StudentEducation saveEducation(StudentEducation education) {
        return studentEducationRepository.save(education);
    }

    public List<StudentEducation> getAllEducation() {
        return studentEducationRepository.findAll();
    }

    public Optional<StudentEducation> getEducationById(Long id) {
        return studentEducationRepository.findById(id);
    }

    public List<StudentEducation> getEducationByStudentId(Long studentId) {
        return studentEducationRepository.findByStudentId(studentId);
    }

    public void deleteEducation(Long id) {
        studentEducationRepository.deleteById(id);
    }
}