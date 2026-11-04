package com.shiwani.placementManagementSystem.controller;

import com.shiwani.placementManagementSystem.entity.PlacementApplication;
import com.shiwani.placementManagementSystem.entity.User;
import com.shiwani.placementManagementSystem.repository.UserRepository;
import com.shiwani.placementManagementSystem.service.PlacementApplicationService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/placement-applications")
public class PlacementApplicationController {

    private final PlacementApplicationService applicationService;
    private final UserRepository userRepository;

    public PlacementApplicationController(
            PlacementApplicationService applicationService,
            UserRepository userRepository) {

        this.applicationService = applicationService;
        this.userRepository = userRepository;
    }

    @PostMapping("/apply/{driveId}")
    public ResponseEntity<PlacementApplication> applyForDrive(
            @PathVariable Long driveId,
            Authentication authentication) {

        requireStudent(authentication);

        User user = getAuthenticatedUser(authentication);

        PlacementApplication application =
                applicationService.applyForDrive(
                        user.getId(),
                        driveId
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(application);
    }

    @GetMapping("/my-applications")
    public ResponseEntity<List<PlacementApplication>> getMyApplications(
            Authentication authentication) {

        requireStudent(authentication);

        User user = getAuthenticatedUser(authentication);

        return ResponseEntity.ok(
                applicationService.getMyApplications(user.getId())
        );
    }

    // Recruiter: own drive applications.
    // Admin: any drive applications.
    @GetMapping("/drive/{driveId}")
    public ResponseEntity<List<PlacementApplication>> getApplicationsForDrive(
            @PathVariable Long driveId,
            Authentication authentication) {

        User user = requireRecruiterOrAdmin(authentication);

        boolean isAdmin =
                "ADMIN".equals(user.getRole().getName());

        return ResponseEntity.ok(
                applicationService.getApplicationsForDrive(
                        driveId,
                        user.getId(),
                        isAdmin
                )
        );
    }

    // Recruiter: own drive applications only.
    // Admin: any application.
    @PatchMapping("/{applicationId}/status")
    public ResponseEntity<PlacementApplication> updateApplicationStatus(
            @PathVariable Long applicationId,
            @RequestBody Map<String, String> request,
            Authentication authentication) {

        User user = requireRecruiterOrAdmin(authentication);

        String status = request.get("status");

        boolean isAdmin =
                "ADMIN".equals(user.getRole().getName());

        PlacementApplication updatedApplication =
                applicationService.updateApplicationStatus(
                        applicationId,
                        status,
                        user.getId(),
                        isAdmin
                );

        return ResponseEntity.ok(updatedApplication);
    }

    private void requireStudent(Authentication authentication) {

        if (authentication == null
                || !authentication.isAuthenticated()) {

            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Authentication is required"
            );
        }

        boolean isStudent = authentication.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch("ROLE_STUDENT"::equals);

        if (!isStudent) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Only students can perform this action"
            );
        }
    }

    private User requireRecruiterOrAdmin(
            Authentication authentication) {

        if (authentication == null
                || !authentication.isAuthenticated()) {

            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Authentication is required"
            );
        }

        User user = getAuthenticatedUser(authentication);

        String roleName = user.getRole().getName();

        if (!"RECRUITER".equals(roleName)
                && !"ADMIN".equals(roleName)) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Only recruiters and admins can perform this action"
            );
        }

        return user;
    }

    private User getAuthenticatedUser(
            Authentication authentication) {

        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Authenticated user not found"
                ));
    }
}