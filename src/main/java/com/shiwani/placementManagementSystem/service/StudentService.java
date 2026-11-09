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

    // Create Student Profile
    @Transactional
    public Student createStudentProfile(StudentRequest request) {

        if (request == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Student details are required"
            );
        }

        if (request.getUserId() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "User ID is required"
            );
        }

        if (request.getName() == null
                || request.getName().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Student name is required"
            );
        }

        if (request.getBranch() == null
                || request.getBranch().isBlank()) {
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
                    "Please enter a valid graduation year"
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

        if (!user.isEnabled()) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "This user account is disabled"
            );
        }

        if (user.getRole() == null
                || !"STUDENT".equals(user.getRole().getName())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Only student accounts can create student profiles"
            );
        }

        Optional<Student> existingStudent =
                studentRepository.findByUserId(user.getId());

        if (existingStudent.isPresent()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Student profile already exists"
            );
        }

        Student student = new Student();

        student.setUser(user);
        student.setName(request.getName().trim());

        student.setPhone(
                request.getPhone() == null
                        ? null
                        : request.getPhone().trim()
        );

        student.setBranch(request.getBranch().trim());
        student.setGraduationYear(request.getGraduationYear());
        student.setCgpa(request.getCgpa());

        student.setBacklogs(
                request.getBacklogs() == null
                        ? 0
                        : request.getBacklogs()
        );

        return studentRepository.save(student);
    }

    // Save Student
    public Student saveStudent(Student student) {
        return studentRepository.save(student);
    }

    // Get All Students
    public List<Student> getAllStudents() {
        return studentRepository.findAll();
    }

    // Get Student By ID
    public Optional<Student> getStudentById(Long id) {
        return studentRepository.findById(id);
    }

    // Get Student By User ID
    public Optional<Student> getStudentByUserId(Long userId) {
        return studentRepository.findByUserId(userId);
    }

    // Get Student By Email
    public Student getStudentByEmail(String email) {

        List<Student> students =
                studentRepository.findAllByUserEmail(email);

        if (students.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Student profile not found"
            );
        }

        return students.get(0);
    }

    // Soft Delete Student
    @Transactional
    public void deleteStudent(Long id) {

        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Student not found"
                ));

        User user = student.getUser();

        if (!user.isEnabled()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Student account is already disabled"
            );
        }

        // Disable account instead of deleting student record
        user.setEnabled(false);
    }

    // Restore Disabled Student Account
    @Transactional
    public void restoreStudent(Long id) {

        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Student not found"
                ));

        User user = student.getUser();

        if (user.isEnabled()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Student account is already active"
            );
        }

        // Enable student account
        user.setEnabled(true);

        userRepository.save(user);
    }
}