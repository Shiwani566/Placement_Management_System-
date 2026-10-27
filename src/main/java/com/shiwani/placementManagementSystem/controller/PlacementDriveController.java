package com.shiwani.placementManagementSystem.controller;

import com.shiwani.placementManagementSystem.entity.PlacementDrive;
import com.shiwani.placementManagementSystem.entity.User;
import com.shiwani.placementManagementSystem.repository.UserRepository;
import com.shiwani.placementManagementSystem.service.PlacementDriveService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/placement-drives")
public class PlacementDriveController {

    private final PlacementDriveService placementDriveService;
    private final UserRepository userRepository;

    public PlacementDriveController(
            PlacementDriveService placementDriveService,
            UserRepository userRepository) {

        this.placementDriveService = placementDriveService;
        this.userRepository = userRepository;
    }

    @PostMapping
    public ResponseEntity<PlacementDrive> createPlacementDrive(
            @RequestBody PlacementDrive placementDrive,
            Authentication authentication) {

        User user = getAuthenticatedUser(authentication);
        String roleName = user.getRole().getName();

        if (!"RECRUITER".equals(roleName)
                && !"ADMIN".equals(roleName)) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Only recruiters and admins can create placement drives"
            );
        }

        PlacementDrive savedDrive;

        if ("RECRUITER".equals(roleName)) {
            savedDrive = placementDriveService.createPlacementDrive(
                    placementDrive,
                    user.getEmail()
            );
        } else {
            placementDrive.setId(null);
            placementDrive.setRecruiter(null);

            savedDrive = placementDriveService.savePlacementDrive(
                    placementDrive
            );
        }

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(savedDrive);
    }

    @GetMapping
    public ResponseEntity<List<PlacementDrive>> getAllPlacementDrives() {

        return ResponseEntity.ok(
                placementDriveService.getAllPlacementDrives()
        );
    }

    @GetMapping("/my-drives")
    public ResponseEntity<List<PlacementDrive>> getMyDrives(
            Authentication authentication) {

        User user = getAuthenticatedUser(authentication);

        if (!"RECRUITER".equals(user.getRole().getName())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Only recruiters can view their own drives"
            );
        }

        return ResponseEntity.ok(
                placementDriveService.getRecruiterPlacementDrives(
                        user.getId()
                )
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<PlacementDrive> getPlacementDriveById(
            @PathVariable Long id) {

        PlacementDrive placementDrive =
                placementDriveService.getPlacementDriveById(id)
                        .orElseThrow(() -> new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Placement drive not found"
                        ));

        return ResponseEntity.ok(placementDrive);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PlacementDrive> updatePlacementDrive(
            @PathVariable Long id,
            @RequestBody PlacementDrive placementDrive) {

        PlacementDrive updatedDrive =
                placementDriveService.updatePlacementDrive(
                        id,
                        placementDrive
                );

        return ResponseEntity.ok(updatedDrive);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePlacementDrive(
            @PathVariable Long id,
            Authentication authentication) {

        User user = getAuthenticatedUser(authentication);
        String roleName = user.getRole().getName();

        if ("ADMIN".equals(roleName)) {
            placementDriveService.deletePlacementDrive(id);
            return ResponseEntity.noContent().build();
        }

        if ("RECRUITER".equals(roleName)) {
            placementDriveService.deleteRecruiterPlacementDrive(
                    id,
                    user.getId()
            );

            return ResponseEntity.noContent().build();
        }

        throw new ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "Only recruiters and admins can delete placement drives"
        );
    }

    private User getAuthenticatedUser(
            Authentication authentication) {

        if (authentication == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Authentication required"
            );
        }

        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Authenticated user not found"
                ));
    }
}
