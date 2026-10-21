package com.shiwani.placementManagementSystem;

import com.shiwani.placementManagementSystem.dto.EligibilityResponse;
import com.shiwani.placementManagementSystem.entity.PlacementApplication;
import com.shiwani.placementManagementSystem.entity.PlacementDrive;
import com.shiwani.placementManagementSystem.entity.Student;
import com.shiwani.placementManagementSystem.repository.PlacementApplicationRepository;
import com.shiwani.placementManagementSystem.repository.PlacementDriveRepository;
import com.shiwani.placementManagementSystem.repository.StudentRepository;
import com.shiwani.placementManagementSystem.service.EligibilityService;
import com.shiwani.placementManagementSystem.service.PlacementApplicationService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PlacementApplicationServiceTest {

    @Mock
    private PlacementApplicationRepository applicationRepository;

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private PlacementDriveRepository placementDriveRepository;

    @Mock
    private EligibilityService eligibilityService;

    @InjectMocks
    private PlacementApplicationService placementApplicationService;


    // Test 1: Duplicate application should be rejected
    @Test
    void shouldRejectDuplicateApplication() {

        Long userId = 1L;
        Long driveId = 1L;

        Student student = new Student();
        student.setId(1L);

        PlacementDrive drive = new PlacementDrive();
        drive.setId(1L);
        drive.setApplicationDeadline(LocalDate.now().plusDays(5));

        EligibilityResponse eligibility = new EligibilityResponse();
        eligibility.setEligible(true);
        eligibility.setReasons(List.of());

        when(studentRepository.findByUserId(userId))
                .thenReturn(Optional.of(student));

        when(placementDriveRepository.findById(driveId))
                .thenReturn(Optional.of(drive));

        when(eligibilityService.checkEligibility(userId, driveId))
                .thenReturn(eligibility);

        when(applicationRepository.existsByStudentIdAndPlacementDriveId(1L, 1L))
                .thenReturn(true);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> placementApplicationService.applyForDrive(userId, driveId)
        );

        assertEquals(409, exception.getStatusCode().value());

        assertEquals(
                "You have already applied for this placement drive",
                exception.getReason()
        );

        verify(applicationRepository, never())
                .save(any(PlacementApplication.class));
    }


    // Test 2: Application status should update to SHORTLISTED
    @Test
    void shouldUpdateApplicationStatusToShortlisted() {

        Long applicationId = 1L;

        PlacementApplication application = new PlacementApplication();
        application.setId(applicationId);
        application.setStatus("APPLIED");

        when(applicationRepository.findById(applicationId))
                .thenReturn(Optional.of(application));

        when(applicationRepository.save(any(PlacementApplication.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        PlacementApplication updatedApplication =
                placementApplicationService.updateApplicationStatus(
                        applicationId,
                        "SHORTLISTED"
                );

        assertEquals(
                "SHORTLISTED",
                updatedApplication.getStatus()
        );

        verify(applicationRepository).save(application);
    }
}