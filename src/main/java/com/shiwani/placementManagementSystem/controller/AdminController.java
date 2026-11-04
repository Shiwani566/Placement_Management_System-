package com.shiwani.placementManagementSystem.controller;

import com.shiwani.placementManagementSystem.dto.RegisterRequest;
import com.shiwani.placementManagementSystem.entity.User;
import com.shiwani.placementManagementSystem.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final UserService userService;

    public AdminController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/recruiters")
    public ResponseEntity<Map<String, Object>> registerRecruiter(
            @RequestBody RegisterRequest request) {

        User user = userService.registerRecruiter(request);

        Map<String, Object> response = Map.of(
                "message", "Recruiter registered successfully",
                "userId", user.getId(),
                "email", user.getEmail(),
                "role", user.getRole().getName()
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }
}