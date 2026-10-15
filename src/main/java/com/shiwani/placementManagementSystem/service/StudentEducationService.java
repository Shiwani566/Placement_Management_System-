package com.shiwani.placementManagementSystem.service;

import com.shiwani.placementManagementSystem.entity.Student;
import com.shiwani.placementManagementSystem.entity.StudentEducation;
import com.shiwani.placementManagementSystem.repository.StudentEducationRepository;
import com.shiwani.placementManagementSystem.repository.StudentRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class StudentEducationService {

    private final StudentEducationRepository studentEducationRepository;
    private final StudentRepository studentRepository;

    public StudentEducationService(
            StudentEducationRepository studentEducationRepository,
            StudentRepository studentRepository) {
        this.studentEducationRepository = studentEducationRepository;
        this.studentRepository = studentRepository;
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

    public boolean isStudentOwnedByUser(Long studentId, Long userId) {
        Optional<Student> student = studentRepository.findById(studentId);

        return student
                .map(profile -> profile.getUser().getId().equals(userId))
                .orElse(false);
    }

    public void deleteEducation(Long id) {
        studentEducationRepository.deleteById(id);
    }
}

