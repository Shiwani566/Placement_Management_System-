package com.shiwani.placementManagementSystem.repository;

import com.shiwani.placementManagementSystem.entity.PlacementDrive;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlacementDriveRepository extends JpaRepository<PlacementDrive, Long> {

    // Existing methods - required by existing services
    List<PlacementDrive> findByRecruiterId(Long recruiterId);

    Optional<PlacementDrive> findByIdAndRecruiterId(Long id, Long recruiterId);


    // Soft-delete methods
    List<PlacementDrive> findAllByActiveTrue();

    List<PlacementDrive> findAllByOrderByIdDesc();

    List<PlacementDrive> findByRecruiterIdAndActiveTrue(Long recruiterId);

    Optional<PlacementDrive> findByIdAndActiveTrue(Long id);

    Optional<PlacementDrive> findByIdAndRecruiterIdAndActiveTrue(
            Long id, Long recruiterId
    );
}