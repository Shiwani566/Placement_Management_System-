package com.shiwani.placementManagementSystem.controller;

import com.shiwani.placementManagementSystem.dto.LoginRequest;
import com.shiwani.placementManagementSystem.dto.LoginResponse;
import com.shiwani.placementManagementSystem.dto.RegisterRequest;
import com.shiwani.placementManagementSystem.dto.ForgotPasswordRequest;
import com.shiwani.placementManagementSystem.dto.ResetPasswordRequest;

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

    // Student Registration
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

    // Login
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @RequestBody LoginRequest request) {

        LoginResponse response = userService.login(request);

        return ResponseEntity.ok(response);
    }

    // Forgot Password
    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, String>> forgotPassword(
            @RequestBody ForgotPasswordRequest request) {

        userService.forgotPassword(request.getEmail());

        return ResponseEntity.ok(Map.of(
                "message",
                "If an account exists with this email, a password reset link will be sent."
        ));
    }

    // Reset Password
    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(
            @RequestBody ResetPasswordRequest request) {

        userService.resetPassword(
                request.getToken(),
                request.getNewPassword()
        );

        return ResponseEntity.ok(Map.of(
                "message",
                "Password reset successfully"
        ));
    }
}