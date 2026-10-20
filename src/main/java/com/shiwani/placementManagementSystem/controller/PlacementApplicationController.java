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

        // Temporary debugging logs
        System.out.println("=== APPLY REQUEST DEBUG ===");
        System.out.println("Authentication: " + authentication);

        System.out.println(
                "Authorities: " +
                        (authentication == null
                                ? "null"
                                : authentication.getAuthorities())
        );

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

    @GetMapping("/drive/{driveId}")
    public ResponseEntity<List<PlacementApplication>> getApplicationsForDrive(
            @PathVariable Long driveId,
            Authentication authentication) {

        requireAdmin(authentication);

        return ResponseEntity.ok(
                applicationService.getApplicationsForDrive(driveId)
        );
    }

    @PatchMapping("/{applicationId}/status")
    public ResponseEntity<PlacementApplication> updateApplicationStatus(
            @PathVariable Long applicationId,
            @RequestBody Map<String, String> request,
            Authentication authentication) {

        requireAdmin(authentication);

        String status = request.get("status");

        PlacementApplication updatedApplication =
                applicationService.updateApplicationStatus(
                        applicationId,
                        status
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

    private void requireAdmin(Authentication authentication) {

        if (authentication == null
                || !authentication.isAuthenticated()) {

            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Authentication is required"
            );
        }

        boolean isAdmin = authentication.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch("ROLE_ADMIN"::equals);

        if (!isAdmin) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Only admins can perform this action"
            );
        }
    }

    private User getAuthenticatedUser(Authentication authentication) {

        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Authenticated user not found"
                ));
    }
}
