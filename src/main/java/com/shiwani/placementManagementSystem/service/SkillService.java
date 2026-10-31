package com.shiwani.placementManagementSystem.service;

import com.shiwani.placementManagementSystem.entity.Skill;
import com.shiwani.placementManagementSystem.repository.SkillRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Service
public class SkillService {

    private final SkillRepository skillRepository;

    public SkillService(SkillRepository skillRepository) {
        this.skillRepository = skillRepository;
    }

    public Skill saveSkill(Skill skill) {

        String skillName = skill.getName().trim();
        skill.setName(skillName);

        if (skillRepository.findByName(skillName).isPresent()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Skill already exists"
            );
        }

        try {
            return skillRepository.save(skill);
        } catch (DataIntegrityViolationException e) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Skill already exists"
            );
        }
    }

    public List<Skill> getAllSkills() {
        return skillRepository.findAll();
    }

    public Optional<Skill> getSkillById(Long id) {
        return skillRepository.findById(id);
    }

    public Optional<Skill> getSkillByName(String name) {
        return skillRepository.findByName(name);
    }

    public void deleteSkill(Long id) {
        skillRepository.deleteById(id);
    }
}