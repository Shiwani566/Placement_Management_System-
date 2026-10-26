package com.shiwani.placementManagementSystem.service;

import com.shiwani.placementManagementSystem.entity.Skill;
import com.shiwani.placementManagementSystem.entity.Student;
import com.shiwani.placementManagementSystem.entity.StudentSkill;
import com.shiwani.placementManagementSystem.entity.User;
import com.shiwani.placementManagementSystem.repository.SkillRepository;
import com.shiwani.placementManagementSystem.repository.StudentRepository;
import com.shiwani.placementManagementSystem.repository.StudentSkillRepository;
import com.shiwani.placementManagementSystem.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Service
public class StudentSkillService {

    private final StudentSkillRepository studentSkillRepository;
    private final StudentRepository studentRepository;
    private final SkillRepository skillRepository;
    private final UserRepository userRepository;

    public StudentSkillService(
            StudentSkillRepository studentSkillRepository,
            StudentRepository studentRepository,
            SkillRepository skillRepository,
            UserRepository userRepository) {

        this.studentSkillRepository = studentSkillRepository;
        this.studentRepository = studentRepository;
        this.skillRepository = skillRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public StudentSkill saveStudentSkill(
            StudentSkill request,
            Authentication authentication) {

        boolean isAdmin = hasRole(authentication, "ROLE_ADMIN");
        boolean isStudent = hasRole(authentication, "ROLE_STUDENT");

        if (!isAdmin && !isStudent) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Only students and admins can add student skills"
            );
        }

        Student student;

        if (isStudent) {

            student = getLoggedInStudent(authentication);

            if (request.getStudent() != null
                    && request.getStudent().getId() != null
                    && !student.getId().equals(
                    request.getStudent().getId())) {

                throw new ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "You can add skills only to your own profile"
                );
            }

        } else {

            if (request.getStudent() == null
                    || request.getStudent().getId() == null) {

                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Student ID is required"
                );
            }

            student = studentRepository
                    .findById(request.getStudent().getId())
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "Student not found"
                    ));
        }

        if (request.getSkill() == null
                || request.getSkill().getId() == null) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Skill ID is required"
            );
        }

        Skill skill = skillRepository
                .findById(request.getSkill().getId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Skill not found"
                ));

        if (studentSkillRepository.existsByStudentIdAndSkillId(
                student.getId(),
                skill.getId())) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "This skill is already assigned to the student"
            );
        }

        StudentSkill studentSkill = new StudentSkill(student, skill);

        return studentSkillRepository.save(studentSkill);
    }

    public List<StudentSkill> getAllStudentSkills(
            Authentication authentication) {

        requireAdminOrRecruiter(authentication);

        return studentSkillRepository.findAll();
    }

    public Optional<StudentSkill> getStudentSkillById(
            Long id,
            Authentication authentication) {

        StudentSkill studentSkill = studentSkillRepository
                .findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Student skill mapping not found"
                ));

        checkStudentOwnership(studentSkill, authentication);

        return Optional.of(studentSkill);
    }

    public List<StudentSkill> getSkillsByStudentId(
            Long studentId,
            Authentication authentication) {

        if (hasRole(authentication, "ROLE_STUDENT")) {

            Student loggedInStudent =
                    getLoggedInStudent(authentication);

            if (!loggedInStudent.getId().equals(studentId)) {
                throw new ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "You cannot access another student's skills"
                );
            }

        } else {
            requireAdminOrRecruiter(authentication);
        }

        if (!studentRepository.existsById(studentId)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Student not found"
            );
        }

        return studentSkillRepository.findByStudentId(studentId);
    }

    public List<StudentSkill> getStudentsBySkillId(
            Long skillId,
            Authentication authentication) {

        requireAdminOrRecruiter(authentication);

        if (!skillRepository.existsById(skillId)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Skill not found"
            );
        }

        return studentSkillRepository.findBySkillId(skillId);
    }

    @Transactional
    public void deleteStudentSkill(
            Long id,
            Authentication authentication) {

        boolean isAdmin = hasRole(authentication, "ROLE_ADMIN");
        boolean isStudent = hasRole(authentication, "ROLE_STUDENT");

        if (!isAdmin && !isStudent) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Only students and admins can delete student skills"
            );
        }

        StudentSkill studentSkill = studentSkillRepository
                .findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Student skill mapping not found"
                ));

        checkStudentOwnership(studentSkill, authentication);

        studentSkillRepository.delete(studentSkill);
    }

    private Student getLoggedInStudent(
            Authentication authentication) {

        if (authentication == null
                || !authentication.isAuthenticated()) {

            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Authentication is required"
            );
        }

        User user = userRepository
                .findByEmail(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Authenticated user not found"
                ));

        return studentRepository
                .findByUserId(user.getId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Student profile not found"
                ));
    }

    private void checkStudentOwnership(
            StudentSkill studentSkill,
            Authentication authentication) {

        if (hasRole(authentication, "ROLE_STUDENT")) {

            Student loggedInStudent =
                    getLoggedInStudent(authentication);

            if (!loggedInStudent.getId().equals(
                    studentSkill.getStudent().getId())) {

                throw new ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "You cannot access another student's skill mapping"
                );
            }

        } else {
            requireAdminOrRecruiter(authentication);
        }
    }

    private void requireAdminOrRecruiter(
            Authentication authentication) {

        if (!hasRole(authentication, "ROLE_ADMIN")
                && !hasRole(authentication, "ROLE_RECRUITER")) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Only admins and recruiters can perform this action"
            );
        }
    }

    private boolean hasRole(
            Authentication authentication,
            String role) {

        return authentication != null
                && authentication.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(role::equals);
    }
}