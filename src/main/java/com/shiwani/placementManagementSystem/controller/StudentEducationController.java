package com.shiwani.placementManagementSystem.controller;

import com.shiwani.placementManagementSystem.entity.StudentEducation;
import com.shiwani.placementManagementSystem.service.StudentEducationService;
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
    public ResponseEntity<StudentEducation> createEducation(
            @RequestBody StudentEducation education) {
        return ResponseEntity.ok(
                studentEducationService.saveEducation(education)
        );
    }

    @GetMapping
    public ResponseEntity<List<StudentEducation>> getAllEducation() {
        return ResponseEntity.ok(
                studentEducationService.getAllEducation()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<StudentEducation> getEducationById(
            @PathVariable Long id) {

        return studentEducationService.getEducationById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<StudentEducation>> getEducationByStudentId(
            @PathVariable Long studentId) {

        return ResponseEntity.ok(
                studentEducationService.getEducationByStudentId(studentId)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEducation(
            @PathVariable Long id) {

        studentEducationService.deleteEducation(id);
        return ResponseEntity.noContent().build();
    }
}