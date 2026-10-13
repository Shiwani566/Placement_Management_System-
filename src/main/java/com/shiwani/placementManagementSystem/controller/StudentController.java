 package com.shiwani.placementManagementSystem.controller;

import com.shiwani.placementManagementSystem.dto.StudentRequest;
import com.shiwani.placementManagementSystem.dto.StudentResponse;
import com.shiwani.placementManagementSystem.entity.Student;
import com.shiwani.placementManagementSystem.entity.User;
import com.shiwani.placementManagementSystem.repository.UserRepository;
import com.shiwani.placementManagementSystem.service.StudentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/students")
public class StudentController {

    private final StudentService studentService;
    private final UserRepository userRepository;

    public StudentController(
            StudentService studentService,
            UserRepository userRepository) {

        this.studentService = studentService;
        this.userRepository = userRepository;
    }

    @PostMapping
    public ResponseEntity<StudentResponse> createStudent(
            @RequestBody StudentRequest request,
            Authentication authentication) {

        boolean isAdmin = hasRole(authentication, "ROLE_ADMIN");

        if (!isAdmin) {
            Long loggedInUserId = getLoggedInUserId(authentication);

            if (!loggedInUserId.equals(request.getUserId())) {
                throw new ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "You can create a profile only for your own account"
                );
            }
        }

        Student student = studentService.createStudentProfile(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(convertToResponse(student));
    }

    @GetMapping
    public ResponseEntity<List<StudentResponse>> getAllStudents() {

        List<StudentResponse> students = studentService.getAllStudents()
                .stream()
                .map(this::convertToResponse)
                .toList();

        return ResponseEntity.ok(students);
    }

    @GetMapping("/{id}")
    public ResponseEntity<StudentResponse> getStudentById(
            @PathVariable Long id,
            Authentication authentication) {

        Student student = studentService.getStudentById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Student profile not found"
                ));

        if (!hasRole(authentication, "ROLE_ADMIN")) {
            Long loggedInUserId = getLoggedInUserId(authentication);

            if (!loggedInUserId.equals(student.getUser().getId())) {
                throw new ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "You cannot access another student's profile"
                );
            }
        }

        return ResponseEntity.ok(convertToResponse(student));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<StudentResponse> getStudentByUserId(
            @PathVariable Long userId,
            Authentication authentication) {

        if (!hasRole(authentication, "ROLE_ADMIN")) {
            Long loggedInUserId = getLoggedInUserId(authentication);

            if (!loggedInUserId.equals(userId)) {
                throw new ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "You cannot access another user's profile"
                );
            }
        }

        return studentService.getStudentByUserId(userId)
                .map(this::convertToResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStudent(@PathVariable Long id) {

        studentService.deleteStudent(id);

        return ResponseEntity.noContent().build();
    }

    private Long getLoggedInUserId(Authentication authentication) {

        return userRepository.findByEmail(authentication.getName())
                .map(User::getId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Authenticated user not found"
                ));
    }

    private boolean hasRole(
            Authentication authentication,
            String role) {

        return authentication.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(role::equals);
    }

    private StudentResponse convertToResponse(Student student) {

        StudentResponse response = new StudentResponse();

        response.setId(student.getId());
        response.setUserId(student.getUser().getId());
        response.setName(student.getName());
        response.setBranch(student.getBranch());
        response.setGraduationYear(student.getGraduationYear());
        response.setCgpa(student.getCgpa());
        response.setBacklogs(student.getBacklogs());
        response.setPhone(student.getPhone());

        return response;
    }
}

