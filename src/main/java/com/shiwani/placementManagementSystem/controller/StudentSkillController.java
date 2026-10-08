package com.shiwani.placementManagementSystem.controller;

import com.shiwani.placementManagementSystem.entity.StudentSkill;
import com.shiwani.placementManagementSystem.service.StudentSkillService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/student-skills")
public class StudentSkillController {

    private final StudentSkillService studentSkillService;

    public StudentSkillController(StudentSkillService studentSkillService) {
        this.studentSkillService = studentSkillService;
    }

    @PostMapping
    public ResponseEntity<StudentSkill> createStudentSkill(
            @RequestBody StudentSkill studentSkill) {

        return ResponseEntity.ok(
                studentSkillService.saveStudentSkill(studentSkill)
        );
    }

    @GetMapping
    public ResponseEntity<List<StudentSkill>> getAllStudentSkills() {
        return ResponseEntity.ok(
                studentSkillService.getAllStudentSkills()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<StudentSkill> getStudentSkillById(
            @PathVariable Long id) {

        return studentSkillService.getStudentSkillById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<StudentSkill>> getSkillsByStudentId(
            @PathVariable Long studentId) {

        return ResponseEntity.ok(
                studentSkillService.getSkillsByStudentId(studentId)
        );
    }

    @GetMapping("/skill/{skillId}")
    public ResponseEntity<List<StudentSkill>> getStudentsBySkillId(
            @PathVariable Long skillId) {

        return ResponseEntity.ok(
                studentSkillService.getStudentsBySkillId(skillId)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStudentSkill(
            @PathVariable Long id) {

        studentSkillService.deleteStudentSkill(id);
        return ResponseEntity.noContent().build();
    }
}