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

        return placementDriveRepository.save(placementDrive);
    }

    @Transactional
    public PlacementDrive savePlacementDrive(
            PlacementDrive placementDrive) {

        validatePlacementDrive(placementDrive);

        return placementDriveRepository.save(placementDrive);
    }

    @Transactional
    public PlacementDrive updatePlacementDrive(
            Long id,
            PlacementDrive updatedDrive) {

        validatePlacementDrive(updatedDrive);

        PlacementDrive existingDrive =
                placementDriveRepository.findById(id)
                        .orElseThrow(() -> new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Placement drive not found"
                        ));

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

    @Transactional(readOnly = true)
    public List<PlacementDrive> getAllPlacementDrives() {

        return placementDriveRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<PlacementDrive> getRecruiterPlacementDrives(
            Long recruiterId) {

        return placementDriveRepository.findByRecruiterId(recruiterId);
    }

    @Transactional(readOnly = true)
    public Optional<PlacementDrive> getPlacementDriveById(Long id) {

        return placementDriveRepository.findById(id);
    }

    @Transactional
    public void deleteRecruiterPlacementDrive(
            Long driveId,
            Long recruiterId) {

        PlacementDrive placementDrive =
                placementDriveRepository
                        .findByIdAndRecruiterId(driveId, recruiterId)
                        .orElseThrow(() -> new ResponseStatusException(
                                HttpStatus.FORBIDDEN,
                                "You can delete only your own placement drives"
                        ));

        placementDriveRepository.delete(placementDrive);
    }

    @Transactional
    public void deletePlacementDrive(Long id) {

        if (!placementDriveRepository.existsById(id)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Placement drive not found"
            );
        }

        placementDriveRepository.deleteById(id);
    }
}