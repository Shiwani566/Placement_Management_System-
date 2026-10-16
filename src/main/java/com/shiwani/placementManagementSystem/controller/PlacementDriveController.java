package com.shiwani.placementManagementSystem.controller;

import com.shiwani.placementManagementSystem.entity.PlacementDrive;
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

    public PlacementDriveController(
            PlacementDriveService placementDriveService) {
        this.placementDriveService = placementDriveService;
    }

    // Create a placement drive
    @PostMapping
    public ResponseEntity<PlacementDrive> createPlacementDrive(
            @RequestBody PlacementDrive placementDrive) {

        PlacementDrive savedDrive =
                placementDriveService.savePlacementDrive(placementDrive);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(savedDrive);
    }

    // Get all placement drives
    @GetMapping
    public ResponseEntity<List<PlacementDrive>> getAllPlacementDrives() {

        return ResponseEntity.ok(
                placementDriveService.getAllPlacementDrives()
        );
    }

    // Get a placement drive by ID
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

    // Delete a placement drive
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePlacementDrive(
            @PathVariable Long id) {

        if (placementDriveService.getPlacementDriveById(id).isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Placement drive not found"
            );
        }

        placementDriveService.deletePlacementDrive(id);

        return ResponseEntity.noContent().build();
    }
}