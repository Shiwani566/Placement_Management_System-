package com.shiwani.placementManagementSystem.controller;

import com.shiwani.placementManagementSystem.dto.EligibilityResponse;
import com.shiwani.placementManagementSystem.entity.User;
import com.shiwani.placementManagementSystem.repository.UserRepository;
import com.shiwani.placementManagementSystem.service.EligibilityService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/placement-drives")
public class EligibilityController {


    private final EligibilityService eligibilityService;
    private final UserRepository userRepository;

    public EligibilityController(
            EligibilityService eligibilityService,
            UserRepository userRepository) {

        this.eligibilityService = eligibilityService;
        this.userRepository = userRepository;
    }

    @GetMapping("/{driveId}/eligibility")
    public ResponseEntity<EligibilityResponse> checkEligibility(
            @PathVariable Long driveId,
            Authentication authentication) {

        boolean isStudent = authentication.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch("ROLE_STUDENT"::equals);

        if (!isStudent) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Only students can check their own eligibility"
            );
        }

        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Authenticated user not found"
                ));

        EligibilityResponse response =
                eligibilityService.checkEligibility(user.getId(), driveId);

        return ResponseEntity.ok(response);
    }


}
