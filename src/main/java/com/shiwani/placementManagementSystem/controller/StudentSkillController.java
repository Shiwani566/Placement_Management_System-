package com.shiwani.placementManagementSystem.controller;

import com.shiwani.placementManagementSystem.dto.StudentSkillResponse;
import com.shiwani.placementManagementSystem.entity.StudentSkill;
import com.shiwani.placementManagementSystem.service.StudentSkillService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/student-skills")
public class StudentSkillController {

    private final StudentSkillService studentSkillService;

    public StudentSkillController(
            StudentSkillService studentSkillService) {

        this.studentSkillService = studentSkillService;
    }

    @PostMapping
    public ResponseEntity<StudentSkillResponse> createStudentSkill(
            @RequestBody StudentSkill studentSkill,
            Authentication authentication) {

        StudentSkill savedStudentSkill =
                studentSkillService.saveStudentSkill(
                        studentSkill,
                        authentication
                );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(convertToResponse(savedStudentSkill));
    }

    @GetMapping
    public ResponseEntity<List<StudentSkillResponse>> getAllStudentSkills(
            Authentication authentication) {

        List<StudentSkillResponse> responseList =
                studentSkillService
                        .getAllStudentSkills(authentication)
                        .stream()
                        .map(this::convertToResponse)
                        .toList();

        return ResponseEntity.ok(responseList);
    }

    @GetMapping("/{id}")
    public ResponseEntity<StudentSkillResponse> getStudentSkillById(
            @PathVariable Long id,
            Authentication authentication) {

        return studentSkillService
                .getStudentSkillById(id, authentication)
                .map(this::convertToResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<StudentSkillResponse>> getSkillsByStudentId(
            @PathVariable Long studentId,
            Authentication authentication) {

        List<StudentSkillResponse> responseList =
                studentSkillService
                        .getSkillsByStudentId(
                                studentId,
                                authentication
                        )
                        .stream()
                        .map(this::convertToResponse)
                        .toList();

        return ResponseEntity.ok(responseList);
    }

    @GetMapping("/skill/{skillId}")
    public ResponseEntity<List<StudentSkillResponse>> getStudentsBySkillId(
            @PathVariable Long skillId,
            Authentication authentication) {

        List<StudentSkillResponse> responseList =
                studentSkillService
                        .getStudentsBySkillId(
                                skillId,
                                authentication
                        )
                        .stream()
                        .map(this::convertToResponse)
                        .toList();

        return ResponseEntity.ok(responseList);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStudentSkill(
            @PathVariable Long id,
            Authentication authentication) {

        studentSkillService.deleteStudentSkill(id, authentication);

        return ResponseEntity.noContent().build();
    }

    private StudentSkillResponse convertToResponse(
            StudentSkill studentSkill) {

        StudentSkillResponse response = new StudentSkillResponse();

        response.setId(studentSkill.getId());
        response.setStudentId(studentSkill.getStudent().getId());
        response.setSkillId(studentSkill.getSkill().getId());
        response.setSkillName(studentSkill.getSkill().getName());

        return response;
    }
}

