package com.shiwani.placementManagementSystem.repository;

import com.shiwani.placementManagementSystem.entity.PlacementApplication;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PlacementApplicationRepository
        extends JpaRepository<PlacementApplication, Long> {

    boolean existsByStudentIdAndPlacementDriveId(
            Long studentId,
            Long placementDriveId
    );

    List<PlacementApplication> findByStudentId(Long studentId);

    List<PlacementApplication> findByPlacementDriveId(Long placementDriveId);
}

