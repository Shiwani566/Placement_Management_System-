 package com.shiwani.placementManagementSystem.controller;

import com.shiwani.placementManagementSystem.dto.StudentEducationResponse;
import com.shiwani.placementManagementSystem.entity.StudentEducation;
import com.shiwani.placementManagementSystem.service.StudentEducationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/education")
public class StudentEducationController {

    private final StudentEducationService studentEducationService;

    public StudentEducationController(StudentEducationService studentEducationService) {
        this.studentEducationService = studentEducationService;
    }

    @PostMapping
    public ResponseEntity<StudentEducationResponse> createEducation(
            @RequestBody StudentEducation education) {

        StudentEducation savedEducation =
                studentEducationService.saveEducation(education);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(convertToResponse(savedEducation));
    }

    @GetMapping
    public ResponseEntity<List<StudentEducationResponse>> getAllEducation() {

        List<StudentEducationResponse> educationList =
                studentEducationService.getAllEducation()
                        .stream()
                        .map(this::convertToResponse)
                        .toList();

        return ResponseEntity.ok(educationList);
    }

    @GetMapping("/{id}")
    public ResponseEntity<StudentEducationResponse> getEducationById(
            @PathVariable Long id) {

        return studentEducationService.getEducationById(id)
                .map(this::convertToResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<StudentEducationResponse>> getEducationByStudentId(
            @PathVariable Long studentId) {

        List<StudentEducationResponse> educationList =
                studentEducationService.getEducationByStudentId(studentId)
                        .stream()
                        .map(this::convertToResponse)
                        .toList();

        return ResponseEntity.ok(educationList);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEducation(@PathVariable Long id) {

        studentEducationService.deleteEducation(id);
        return ResponseEntity.noContent().build();
    }

    private StudentEducationResponse convertToResponse(StudentEducation education) {

        StudentEducationResponse response = new StudentEducationResponse();

        response.setId(education.getId());
        response.setStudentId(education.getStudent().getId());
        response.setQualification(education.getQualification());
        response.setInstitution(education.getInstitution());
        response.setBoardOrUniversity(education.getBoardOrUniversity());
        response.setPassingYear(education.getPassingYear());
        response.setPercentage(education.getPercentage());

        return response;
    }
}
