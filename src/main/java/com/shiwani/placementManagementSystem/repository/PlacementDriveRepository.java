package com.shiwani.placementManagementSystem.repository;

import com.shiwani.placementManagementSystem.entity.PlacementDrive;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlacementDriveRepository extends JpaRepository<PlacementDrive, Long> {

    List<PlacementDrive> findByRecruiterId(Long recruiterId);

    Optional<PlacementDrive> findByIdAndRecruiterId(
            Long id,
            Long recruiterId
    );
}

