package com.shiwani.placementManagementSystem.service;

import com.shiwani.placementManagementSystem.dto.StudentRequest;
import com.shiwani.placementManagementSystem.entity.Student;
import com.shiwani.placementManagementSystem.entity.User;
import com.shiwani.placementManagementSystem.repository.StudentRepository;
import com.shiwani.placementManagementSystem.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Service
public class StudentService {

    private final StudentRepository studentRepository;
    private final UserRepository userRepository;

    public StudentService(
            StudentRepository studentRepository,
            UserRepository userRepository) {

        this.studentRepository = studentRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public Student createStudentProfile(StudentRequest request) {

        if (request == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Request body is required"
            );
        }

        if (request.getUserId() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "User ID is required"
            );
        }

        if (request.getName() == null || request.getName().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Student name is required"
            );
        }

        if (request.getBranch() == null || request.getBranch().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Branch is required"
            );
        }

        if (request.getGraduationYear() == null
                || request.getGraduationYear() < 2000
                || request.getGraduationYear() > 2100) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Graduation year must be between 2000 and 2100"
            );
        }

        if (request.getCgpa() == null
                || !Double.isFinite(request.getCgpa())
                || request.getCgpa() < 0
                || request.getCgpa() > 10) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "CGPA must be between 0 and 10"
            );
        }

        if (request.getBacklogs() != null
                && request.getBacklogs() < 0) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Backlogs cannot be negative"
            );
        }

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "User not found"
                ));

        if (studentRepository.findByUserId(request.getUserId()).isPresent()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Student profile already exists for this user"
            );
        }

        if (user.getRole() == null
                || !"STUDENT".equals(user.getRole().getName())) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "User does not have the STUDENT role"
            );
        }

        Student student = new Student();
        student.setUser(user);
        student.setName(request.getName().trim());
        student.setBranch(request.getBranch().trim());
        student.setGraduationYear(request.getGraduationYear());
        student.setCgpa(request.getCgpa());
        student.setBacklogs(
                request.getBacklogs() == null ? 0 : request.getBacklogs()
        );
        student.setPhone(
                request.getPhone() == null || request.getPhone().isBlank()
                        ? null
                        : request.getPhone().trim()
        );

        return studentRepository.save(student);
    }

    public Student saveStudent(Student student) {
        return studentRepository.save(student);
    }

    public List<Student> getAllStudents() {
        return studentRepository.findAll();
    }

    public Optional<Student> getStudentById(Long id) {
        return studentRepository.findById(id);
    }

    public Optional<Student> getStudentByUserId(Long userId) {
        return studentRepository.findByUserId(userId);
    }

    public Optional<Student> getStudentByEmail(String email) {
        return userRepository.findByEmail(email)
                .flatMap(user ->
                        studentRepository.findByUserId(user.getId())
                );
    }

    public void deleteStudent(Long id) {
        studentRepository.deleteById(id);
    }
}

