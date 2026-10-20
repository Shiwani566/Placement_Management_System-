package com.shiwani.placementManagementSystem.service;

import com.shiwani.placementManagementSystem.dto.EligibilityResponse;
import com.shiwani.placementManagementSystem.entity.PlacementApplication;
import com.shiwani.placementManagementSystem.entity.PlacementDrive;
import com.shiwani.placementManagementSystem.entity.Student;
import com.shiwani.placementManagementSystem.repository.PlacementApplicationRepository;
import com.shiwani.placementManagementSystem.repository.PlacementDriveRepository;
import com.shiwani.placementManagementSystem.repository.StudentRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;

@Service
public class PlacementApplicationService {

    private final PlacementApplicationRepository applicationRepository;
    private final StudentRepository studentRepository;
    private final PlacementDriveRepository placementDriveRepository;
    private final EligibilityService eligibilityService;

    public PlacementApplicationService(
            PlacementApplicationRepository applicationRepository,
            StudentRepository studentRepository,
            PlacementDriveRepository placementDriveRepository,
            EligibilityService eligibilityService) {

        this.applicationRepository = applicationRepository;
        this.studentRepository = studentRepository;
        this.placementDriveRepository = placementDriveRepository;
        this.eligibilityService = eligibilityService;
    }

    @Transactional
    public PlacementApplication applyForDrive(Long userId, Long driveId) {

        // Find the student profile linked to the logged-in user
        Student student = studentRepository.findByUserId(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Student profile not found"
                ));

        // Find the requested placement drive
        PlacementDrive drive = placementDriveRepository.findById(driveId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Placement drive not found"
                ));

        // Check the application deadline
        if (drive.getApplicationDeadline() != null
                && LocalDate.now().isAfter(drive.getApplicationDeadline())) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Application deadline has passed"
            );
        }

        // Check student eligibility
        EligibilityResponse eligibility =
                eligibilityService.checkEligibility(userId, driveId);

        if (!eligibility.isEligible()) {

            String reasons = String.join(
                    "; ",
                    eligibility.getReasons()
            );

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    reasons.isBlank()
                            ? "You are not eligible for this placement drive"
                            : reasons
            );
        }

        // Prevent duplicate applications
        boolean alreadyApplied =
                applicationRepository.existsByStudentIdAndPlacementDriveId(
                        student.getId(),
                        drive.getId()
                );

        if (alreadyApplied) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "You have already applied for this placement drive"
            );
        }

        // Create and save the application
        PlacementApplication application = new PlacementApplication();

        application.setStudent(student);
        application.setPlacementDrive(drive);
        application.setStatus("APPLIED");

        return applicationRepository.save(application);
    }

    @Transactional(readOnly = true)
    public List<PlacementApplication> getMyApplications(Long userId) {

        Student student = studentRepository.findByUserId(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Student profile not found"
                ));

        return applicationRepository.findByStudentId(student.getId());
    }

    @Transactional(readOnly = true)
    public List<PlacementApplication> getApplicationsForDrive(Long driveId) {

        if (!placementDriveRepository.existsById(driveId)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Placement drive not found"
            );
        }

        return applicationRepository.findByPlacementDriveId(driveId);
    }

    @Transactional
    public PlacementApplication updateApplicationStatus(
            Long applicationId,
            String newStatus) {

        PlacementApplication application = applicationRepository
                .findById(applicationId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Application not found"
                ));

        if (newStatus == null
                || !List.of(
                "APPLIED",
                "SHORTLISTED",
                "REJECTED",
                "SELECTED"
        ).contains(newStatus.trim().toUpperCase())) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid application status"
            );
        }

        application.setStatus(newStatus.trim().toUpperCase());

        return applicationRepository.save(application);
    }
}

