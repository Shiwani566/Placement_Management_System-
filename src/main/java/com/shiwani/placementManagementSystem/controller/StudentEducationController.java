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
import java.util.Optional;

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

    // POST: Add education details
    @PostMapping
    public ResponseEntity<StudentEducationResponse> addEducation(
            @RequestBody StudentEducation education,
            Authentication authentication) {

        if (education.getStudent() == null
                || education.getStudent().getId() == null) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Student ID is required"
            );
        }

        Long studentId = education.getStudent().getId();

        // A student can add education only to their own profile.
        if (!hasRole(authentication, "ROLE_ADMIN")) {

            Long loggedInUserId = getLoggedInUserId(authentication);

            boolean ownsProfile =
                    studentEducationService.isStudentOwnedByUser(
                            studentId,
                            loggedInUserId
                    );

            if (!ownsProfile) {
                throw new ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "You cannot add education to another student's profile"
                );
            }
        }

        StudentEducation savedEducation =
                studentEducationService.saveEducation(education);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(convertToResponse(savedEducation));
    }

    // GET: Get all education records (ADMIN only through SecurityConfig)
    @GetMapping
    public ResponseEntity<List<StudentEducationResponse>> getAllEducation() {

        List<StudentEducationResponse> educationList =
                studentEducationService.getAllEducation()
                        .stream()
                        .map(this::convertToResponse)
                        .toList();

        return ResponseEntity.ok(educationList);
    }

    // GET: Get education by education record ID
    @GetMapping("/{id}")
    public ResponseEntity<StudentEducationResponse> getEducationById(
            @PathVariable Long id,
            Authentication authentication) {

        Optional<StudentEducation> educationOptional =
                studentEducationService.getEducationById(id);

        if (educationOptional.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Education record not found"
            );
        }

        StudentEducation education = educationOptional.get();

        // Students can access only their own education records.
        checkOwnership(education, authentication);

        return ResponseEntity.ok(convertToResponse(education));
    }

    // GET: Get education records belonging to a particular student
    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<StudentEducationResponse>> getEducationByStudentId(
            @PathVariable Long studentId,
            Authentication authentication) {

        // Admin can access any student's education records.
        if (!hasRole(authentication, "ROLE_ADMIN")) {

            Long loggedInUserId = getLoggedInUserId(authentication);

            boolean ownsProfile =
                    studentEducationService.isStudentOwnedByUser(
                            studentId,
                            loggedInUserId
                    );

            if (!ownsProfile) {
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

    // DELETE: Delete an education record
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEducation(
            @PathVariable Long id,
            Authentication authentication) {

        Optional<StudentEducation> educationOptional =
                studentEducationService.getEducationById(id);

        if (educationOptional.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Education record not found"
            );
        }

        StudentEducation education = educationOptional.get();

        // Check ownership unless the logged-in user is an admin.
        checkOwnership(education, authentication);

        studentEducationService.deleteEducation(id);

        return ResponseEntity.noContent().build();
    }

    // Helper: Verify ownership of an existing education record
    private void checkOwnership(
            StudentEducation education,
            Authentication authentication) {

        if (hasRole(authentication, "ROLE_ADMIN")) {
            return;
        }

        if (education.getStudent() == null
                || education.getStudent().getUser() == null) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Education record has no valid student ownership"
            );
        }

        Long loggedInUserId = getLoggedInUserId(authentication);

        Long ownerUserId = education.getStudent()
                .getUser()
                .getId();

        if (!ownerUserId.equals(loggedInUserId)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You cannot access another student's education"
            );
        }
    }

    // Helper: Get the logged-in user's database ID
    private Long getLoggedInUserId(Authentication authentication) {

        if (authentication == null
                || authentication.getName() == null) {

            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "User is not authenticated"
            );
        }

        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Authenticated user not found"
                ));

        return user.getId();
    }

    // Helper: Check whether the logged-in user has a particular role
    private boolean hasRole(
            Authentication authentication,
            String roleName) {

        if (authentication == null) {
            return false;
        }

        return authentication.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(roleName::equals);
    }

    // Helper: Convert entity into response DTO
    private StudentEducationResponse convertToResponse(
            StudentEducation education) {

        StudentEducationResponse response =
                new StudentEducationResponse();

        response.setId(education.getId());

        response.setStudentId(
                education.getStudent().getId()
        );

        response.setQualification(
                education.getQualification()
        );

        response.setInstitution(
                education.getInstitution()
        );

        response.setBoardOrUniversity(
                education.getBoardOrUniversity()
        );

        response.setPassingYear(
                education.getPassingYear()
        );

        response.setPercentage(
                education.getPercentage()
        );

        return response;
    }
}