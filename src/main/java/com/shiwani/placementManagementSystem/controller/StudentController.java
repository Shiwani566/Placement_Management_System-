package com.shiwani.placementManagementSystem.controller;

import com.shiwani.placementManagementSystem.dto.StudentRequest;
import com.shiwani.placementManagementSystem.dto.StudentResponse;
import com.shiwani.placementManagementSystem.entity.Student;
import com.shiwani.placementManagementSystem.service.StudentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/students")
public class StudentController {

    private final StudentService studentService;

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    @PostMapping
    public ResponseEntity<StudentResponse> createStudent(
            @RequestBody StudentRequest request) {

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
            @PathVariable Long id) {

        return studentService.getStudentById(id)
                .map(this::convertToResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<StudentResponse> getStudentByUserId(
            @PathVariable Long userId) {

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