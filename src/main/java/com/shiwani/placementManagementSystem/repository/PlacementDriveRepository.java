package com.shiwani.placementManagementSystem.repository;

import com.shiwani.placementManagementSystem.entity.PlacementDrive;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlacementDriveRepository
        extends JpaRepository<PlacementDrive, Long> {

}