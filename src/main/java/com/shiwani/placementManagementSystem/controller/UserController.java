package com.shiwani.placementManagementSystem.controller;

import com.shiwani.placementManagementSystem.dto.RegisterRequest;
import com.shiwani.placementManagementSystem.entity.User;
import com.shiwani.placementManagementSystem.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(
            @RequestBody RegisterRequest request) {

        User user = userService.registerStudent(request);

        Map<String, Object> response = Map.of(
                "message", "Student registered successfully",
                "userId", user.getId(),
                "email", user.getEmail(),
                "role", user.getRole().getName()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}