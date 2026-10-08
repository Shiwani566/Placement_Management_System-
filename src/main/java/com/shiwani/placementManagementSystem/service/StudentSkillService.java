package com.shiwani.placementManagementSystem.service;

import com.shiwani.placementManagementSystem.entity.StudentSkill;
import com.shiwani.placementManagementSystem.repository.StudentSkillRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class StudentSkillService {

    private final StudentSkillRepository studentSkillRepository;

    public StudentSkillService(StudentSkillRepository studentSkillRepository) {
        this.studentSkillRepository = studentSkillRepository;
    }

    public StudentSkill saveStudentSkill(StudentSkill studentSkill) {
        return studentSkillRepository.save(studentSkill);
    }

    public List<StudentSkill> getAllStudentSkills() {
        return studentSkillRepository.findAll();
    }

    public Optional<StudentSkill> getStudentSkillById(Long id) {
        return studentSkillRepository.findById(id);
    }

    public List<StudentSkill> getSkillsByStudentId(Long studentId) {
        return studentSkillRepository.findByStudentId(studentId);
    }

    public List<StudentSkill> getStudentsBySkillId(Long skillId) {
        return studentSkillRepository.findBySkillId(skillId);
    }

    public void deleteStudentSkill(Long id) {
        studentSkillRepository.deleteById(id);
    }
}