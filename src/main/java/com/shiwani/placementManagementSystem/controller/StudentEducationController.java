package com.shiwani.placementManagementSystem.controller;

import com.shiwani.placementManagementSystem.dto.StudentEducationResponse;
import com.shiwani.placementManagementSystem.entity.StudentEducation;
import com.shiwani.placementManagementSystem.entity.User;
import com.shiwani.placementManagementSystem.repository.UserRepository;
import com.shiwani.placementManagementSystem.service.StudentEducationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/education")
public class StudentEducationController {

    private final StudentEducationService studentEducationService;
    private final UserRepository userRepository;

    public StudentEducationController(
            StudentEducationService studentEducationService,
            UserRepository userRepository) {
        this.studentEducationService = studentEducationService;
        this.userRepository = userRepository;
    }

    @PostMapping
    public ResponseEntity<StudentEducationResponse> createEducation(
            @RequestBody StudentEducation education,
            Authentication authentication) {

        boolean isAdmin = hasRole(authentication, "ROLE_ADMIN");

        if (!isAdmin) {
            Long loggedInUserId = getLoggedInUserId(authentication);

            if (education.getStudent() == null
                    || education.getStudent().getUser() == null
                    || !loggedInUserId.equals(
                    education.getStudent().getUser().getId())) {
                throw new ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "You can add education only to your own profile"
                );
            }
        }

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
            @PathVariable Long id,
            Authentication authentication) {

        StudentEducation education =
                studentEducationService.getEducationById(id)
                        .orElseThrow(() -> new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Education record not found"
                        ));

        checkOwnership(education, authentication);

        return ResponseEntity.ok(convertToResponse(education));
    }

    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<StudentEducationResponse>> getEducationByStudentId(
            @PathVariable Long studentId,
            Authentication authentication) {

        boolean isAdmin = hasRole(authentication, "ROLE_ADMIN");

        if (!isAdmin) {
            Long loggedInUserId = getLoggedInUserId(authentication);

            boolean ownsStudentProfile =
                    studentEducationService.getEducationByStudentId(studentId)
                            .stream()
                            .anyMatch(education ->
                                    education.getStudent().getUser().getId()
                                            .equals(loggedInUserId));

            // Empty education records do not prove ownership.
            if (!ownsStudentProfile) {
                throw new ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "You cannot access another student's education"
                );
            }
        }

        List<StudentEducationResponse> educationList =
                studentEducationService.getEducationByStudentId(studentId)
                        .stream()
                        .map(this::convertToResponse)
                        .toList();

        return ResponseEntity.ok(educationList);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEducation(
            @PathVariable Long id,
            Authentication authentication) {

        StudentEducation education =
                studentEducationService.getEducationById(id)
                        .orElseThrow(() -> new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Education record not found"
                        ));

        checkOwnership(education, authentication);

        studentEducationService.deleteEducation(id);

        return ResponseEntity.noContent().build();
    }

    private void checkOwnership(
            StudentEducation education,
            Authentication authentication) {

        if (hasRole(authentication, "ROLE_ADMIN")) {
            return;
        }

        Long loggedInUserId = getLoggedInUserId(authentication);

        if (education.getStudent() == null
                || education.getStudent().getUser() == null
                || !loggedInUserId.equals(
                education.getStudent().getUser().getId())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You cannot access another student's education"
            );
        }
    }

    private Long getLoggedInUserId(Authentication authentication) {

        return userRepository.findByEmail(authentication.getName())
                .map(User::getId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Authenticated user not found"
                ));
    }

    private boolean hasRole(Authentication authentication, String role) {

        return authentication.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(role::equals);
    }

    private StudentEducationResponse convertToResponse(
            StudentEducation education) {

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

