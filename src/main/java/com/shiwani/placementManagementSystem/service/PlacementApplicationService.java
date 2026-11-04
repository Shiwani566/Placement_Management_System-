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

    // Student applies for a placement drive
    @Transactional
    public PlacementApplication applyForDrive(Long userId, Long driveId) {

        Student student = studentRepository.findByUserId(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Student profile not found"
                ));

        PlacementDrive drive = placementDriveRepository.findById(driveId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Placement drive not found"
                ));

        // Check application deadline
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

        PlacementApplication application = new PlacementApplication();

        application.setStudent(student);
        application.setPlacementDrive(drive);
        application.setStatus("APPLIED");

        return applicationRepository.save(application);
    }

    // Student views their own applications
    @Transactional(readOnly = true)
    public List<PlacementApplication> getMyApplications(Long userId) {

        Student student = studentRepository.findByUserId(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Student profile not found"
                ));

        return applicationRepository.findByStudentId(student.getId());
    }

    // Admin can view any drive applications.
    // Recruiter can view applications of own drives only.
    @Transactional(readOnly = true)
    public List<PlacementApplication> getApplicationsForDrive(
            Long driveId,
            Long recruiterId,
            boolean isAdmin) {

        if (isAdmin) {

            if (!placementDriveRepository.existsById(driveId)) {
                throw new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Placement drive not found"
                );
            }

        } else {

            placementDriveRepository
                    .findByIdAndRecruiterId(driveId, recruiterId)
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.FORBIDDEN,
                            "You can view applications only for your own drives"
                    ));
        }

        return applicationRepository.findByPlacementDriveId(driveId);
    }

    // Backward-compatible method for existing service tests.
    // This preserves the previous admin-level service behavior.
    @Transactional
    public PlacementApplication updateApplicationStatus(
            Long applicationId,
            String newStatus) {

        return updateApplicationStatus(
                applicationId,
                newStatus,
                null,
                true
        );
    }

    // Admin can update any application.
    // Recruiter can update applications of own drives only.
    @Transactional
    public PlacementApplication updateApplicationStatus(
            Long applicationId,
            String newStatus,
            Long recruiterId,
            boolean isAdmin) {

        PlacementApplication application = applicationRepository
                .findById(applicationId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Application not found"
                ));

        // Verify recruiter ownership
        if (!isAdmin) {

            Long applicationDriveRecruiterId =
                    application.getPlacementDrive()
                            .getRecruiter() == null
                            ? null
                            : application.getPlacementDrive()
                            .getRecruiter()
                            .getId();

            if (applicationDriveRecruiterId == null
                    || !applicationDriveRecruiterId.equals(recruiterId)) {

                throw new ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "You can update applications only for your own drives"
                );
            }
        }

        // Validate application status
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

