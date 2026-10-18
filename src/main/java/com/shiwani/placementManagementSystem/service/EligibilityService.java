package com.shiwani.placementManagementSystem.service;

import com.shiwani.placementManagementSystem.dto.EligibilityResponse;
import com.shiwani.placementManagementSystem.entity.PlacementDrive;
import com.shiwani.placementManagementSystem.entity.Student;
import com.shiwani.placementManagementSystem.repository.PlacementDriveRepository;
import com.shiwani.placementManagementSystem.repository.StudentRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class EligibilityService {


    private final StudentRepository studentRepository;
    private final PlacementDriveRepository placementDriveRepository;

    public EligibilityService(
            StudentRepository studentRepository,
            PlacementDriveRepository placementDriveRepository) {

        this.studentRepository = studentRepository;
        this.placementDriveRepository = placementDriveRepository;
    }

    public EligibilityResponse checkEligibility(
            Long userId,
            Long driveId) {

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

        List<String> reasons = new ArrayList<>();

        if (student.getCgpa() == null) {
            reasons.add("Student CGPA is not available");
        } else if (drive.getMinimumCgpa() != null
                && student.getCgpa() < drive.getMinimumCgpa()) {
            reasons.add(
                    "Minimum CGPA required: " + drive.getMinimumCgpa()
                            + ", your CGPA: " + student.getCgpa()
            );
        }

        if (student.getBranch() == null
                || student.getBranch().isBlank()) {
            reasons.add("Student branch is not available");
        } else if (drive.getEligibleBranch() != null
                && !drive.getEligibleBranch().isBlank()
                && !student.getBranch().trim()
                .equalsIgnoreCase(drive.getEligibleBranch().trim())) {
            reasons.add(
                    "Eligible branch: " + drive.getEligibleBranch()
                            + ", your branch: " + student.getBranch()
            );
        }

        if (student.getGraduationYear() == null) {
            reasons.add("Student graduation year is not available");
        } else if (drive.getGraduationYear() != null
                && !student.getGraduationYear()
                .equals(drive.getGraduationYear())) {
            reasons.add(
                    "Required graduation year: " + drive.getGraduationYear()
                            + ", your graduation year: "
                            + student.getGraduationYear()
            );
        }

        if (student.getBacklogs() == null) {
            reasons.add("Student backlog information is not available");
        } else if (drive.getMaximumBacklogs() != null
                && student.getBacklogs() > drive.getMaximumBacklogs()) {
            reasons.add(
                    "Maximum backlogs allowed: "
                            + drive.getMaximumBacklogs()
                            + ", your backlogs: " + student.getBacklogs()
            );
        }

        if (drive.getApplicationDeadline() != null
                && LocalDate.now().isAfter(drive.getApplicationDeadline())) {
            reasons.add("Application deadline has passed");
        }

        EligibilityResponse response = new EligibilityResponse();
        response.setStudentId(student.getId());
        response.setPlacementDriveId(drive.getId());
        response.setCompanyName(drive.getCompanyName());
        response.setJobTitle(drive.getJobTitle());
        response.setEligible(reasons.isEmpty());
        response.setReasons(reasons);

        return response;
    }

}
