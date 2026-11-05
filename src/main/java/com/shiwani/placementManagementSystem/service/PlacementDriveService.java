package com.shiwani.placementManagementSystem.service;

import com.shiwani.placementManagementSystem.entity.PlacementDrive;
import com.shiwani.placementManagementSystem.entity.User;
import com.shiwani.placementManagementSystem.repository.PlacementDriveRepository;
import com.shiwani.placementManagementSystem.repository.UserRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Service
public class PlacementDriveService {

    private final PlacementDriveRepository placementDriveRepository;
    private final UserRepository userRepository;

    public PlacementDriveService(
            PlacementDriveRepository placementDriveRepository,
            UserRepository userRepository) {

        this.placementDriveRepository = placementDriveRepository;
        this.userRepository = userRepository;
    }

    // Create placement drive by recruiter
    @Transactional
    public PlacementDrive createPlacementDrive(
            PlacementDrive placementDrive,
            String recruiterEmail) {

        validatePlacementDrive(placementDrive);

        User recruiter = userRepository.findByEmail(recruiterEmail)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Authenticated recruiter not found"
                ));

        placementDrive.setId(null);
        placementDrive.setRecruiter(recruiter);
        placementDrive.setActive(true);

        return placementDriveRepository.save(placementDrive);
    }

    // Save placement drive by admin
    @Transactional
    public PlacementDrive savePlacementDrive(
            PlacementDrive placementDrive) {

        validatePlacementDrive(placementDrive);

        placementDrive.setActive(true);

        return placementDriveRepository.save(placementDrive);
    }

    // Update drive with ownership verification
    @Transactional
    public PlacementDrive updatePlacementDrive(
            Long id,
            PlacementDrive updatedDrive,
            Long recruiterId,
            boolean isAdmin) {

        validatePlacementDrive(updatedDrive);

        PlacementDrive existingDrive;

        if (isAdmin) {

            existingDrive = placementDriveRepository
                    .findByIdAndActiveTrue(id)
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "Active placement drive not found"
                    ));

        } else {

            existingDrive = placementDriveRepository
                    .findByIdAndRecruiterIdAndActiveTrue(id, recruiterId)
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.FORBIDDEN,
                            "You can update only your own active placement drives"
                    ));
        }

        existingDrive.setCompanyName(
                updatedDrive.getCompanyName().trim()
        );

        existingDrive.setJobTitle(
                updatedDrive.getJobTitle().trim()
        );

        existingDrive.setJobDescription(
                updatedDrive.getJobDescription()
        );

        existingDrive.setPackageLpa(
                updatedDrive.getPackageLpa()
        );

        existingDrive.setMinimumCgpa(
                updatedDrive.getMinimumCgpa()
        );

        existingDrive.setEligibleBranch(
                updatedDrive.getEligibleBranch()
        );

        existingDrive.setGraduationYear(
                updatedDrive.getGraduationYear()
        );

        existingDrive.setMaximumBacklogs(
                updatedDrive.getMaximumBacklogs()
        );

        existingDrive.setApplicationDeadline(
                updatedDrive.getApplicationDeadline()
        );

        return placementDriveRepository.save(existingDrive);
    }

    // Validate placement drive details
    private void validatePlacementDrive(
            PlacementDrive placementDrive) {

        if (placementDrive == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Placement drive details are required"
            );
        }

        if (placementDrive.getCompanyName() == null
                || placementDrive.getCompanyName().isBlank()) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Company name is required"
            );
        }

        if (placementDrive.getJobTitle() == null
                || placementDrive.getJobTitle().isBlank()) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Job title is required"
            );
        }

        placementDrive.setCompanyName(
                placementDrive.getCompanyName().trim()
        );

        placementDrive.setJobTitle(
                placementDrive.getJobTitle().trim()
        );
    }

    // Get only active placement drives
    @Transactional(readOnly = true)
    public List<PlacementDrive> getAllPlacementDrives() {

        return placementDriveRepository.findAllByActiveTrue();
    }

    // Admin can view active as well as archived drives
    @Transactional(readOnly = true)
    public List<PlacementDrive> getAllPlacementDrivesForAdmin() {

        return placementDriveRepository.findAllByOrderByIdDesc();
    }

    // Get only active drives created by a recruiter
    @Transactional(readOnly = true)
    public List<PlacementDrive> getRecruiterPlacementDrives(
            Long recruiterId) {

        return placementDriveRepository
                .findByRecruiterIdAndActiveTrue(recruiterId);
    }

    // Get only active placement drive by ID
    @Transactional(readOnly = true)
    public Optional<PlacementDrive> getPlacementDriveById(Long id) {

        return placementDriveRepository.findByIdAndActiveTrue(id);
    }

    // Recruiter can archive only their own active drive
    @Transactional
    public void deleteRecruiterPlacementDrive(
            Long driveId,
            Long recruiterId) {

        PlacementDrive placementDrive =
                placementDriveRepository
                        .findByIdAndRecruiterIdAndActiveTrue(
                                driveId,
                                recruiterId
                        )
                        .orElseThrow(() -> new ResponseStatusException(
                                HttpStatus.FORBIDDEN,
                                "You can archive only your own active placement drives"
                        ));

        placementDrive.setActive(false);
    }

    // Admin can archive any active drive
    @Transactional
    public void deletePlacementDrive(Long id) {

        PlacementDrive placementDrive =
                placementDriveRepository.findById(id)
                        .orElseThrow(() -> new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Placement drive not found"
                        ));

        if (!placementDrive.isActive()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Placement drive is already archived"
            );
        }

        placementDrive.setActive(false);
    }
}