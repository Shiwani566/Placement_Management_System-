package com.shiwani.placementManagementSystem.service;

import com.shiwani.placementManagementSystem.entity.PlacementDrive;
import com.shiwani.placementManagementSystem.repository.PlacementDriveRepository;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PlacementDriveService {

    private final PlacementDriveRepository placementDriveRepository;

    public PlacementDriveService(
            PlacementDriveRepository placementDriveRepository) {
        this.placementDriveRepository = placementDriveRepository;
    }

    // Create or update a placement drive
    public PlacementDrive savePlacementDrive(PlacementDrive placementDrive) {
        return placementDriveRepository.save(placementDrive);
    }

    // Get all placement drives
    public List<PlacementDrive> getAllPlacementDrives() {
        return placementDriveRepository.findAll();
    }

    // Get a placement drive by ID
    public Optional<PlacementDrive> getPlacementDriveById(Long id) {
        return placementDriveRepository.findById(id);
    }

    // Delete a placement drive by ID
    public void deletePlacementDrive(Long id) {
        placementDriveRepository.deleteById(id);
    }
}