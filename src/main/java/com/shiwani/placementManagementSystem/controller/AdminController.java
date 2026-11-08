package com.shiwani.placementManagementSystem.controller;

import com.shiwani.placementManagementSystem.dto.RegisterRequest;
import com.shiwani.placementManagementSystem.entity.*;
import com.shiwani.placementManagementSystem.repository.*;
import com.shiwani.placementManagementSystem.service.UserService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final UserService userService;
    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final PlacementDriveRepository driveRepository;
    private final PlacementApplicationRepository applicationRepository;

    public AdminController(
            UserService userService,
            UserRepository userRepository,
            StudentRepository studentRepository,
            PlacementDriveRepository driveRepository,
            PlacementApplicationRepository applicationRepository) {

        this.userService = userService;
        this.userRepository = userRepository;
        this.studentRepository = studentRepository;
        this.driveRepository = driveRepository;
        this.applicationRepository = applicationRepository;
    }

    // Create recruiter
    @PostMapping("/recruiters")
    public ResponseEntity<Map<String, Object>> registerRecruiter(
            @RequestBody RegisterRequest request) {

        User user = userService.registerRecruiter(request);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Recruiter registered successfully");
        response.put("userId", user.getId());
        response.put("email", user.getEmail());
        response.put("role", user.getRole().getName());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // Dashboard statistics
    @GetMapping("/dashboard/stats")
    public ResponseEntity<Map<String, Object>> getDashboardStats() {

        List<User> users = userRepository.findAll();

        long totalStudents = studentRepository.count();

        long totalRecruiters = users.stream()
                .filter(user -> "RECRUITER".equals(
                        user.getRole().getName()))
                .count();

        long totalDrives = driveRepository.count();

        long totalApplications = applicationRepository.count();

        long totalAdmins = users.stream()
                .filter(user -> "ADMIN".equals(
                        user.getRole().getName()))
                .count();

        Map<String, Object> stats = new HashMap<>();

        stats.put("totalStudents", totalStudents);
        stats.put("totalRecruiters", totalRecruiters);
        stats.put("totalDrives", totalDrives);
        stats.put("totalApplications", totalApplications);
        stats.put("totalAdmins", totalAdmins);

        return ResponseEntity.ok(stats);
    }

    // Get all students (Active + Disabled)
    @GetMapping("/students")
    public ResponseEntity<List<Map<String, Object>>> getAllStudents() {

        List<Map<String, Object>> result = studentRepository.findAll()
                .stream()
                .map(student -> {

                    Map<String, Object> data = new HashMap<>();

                    data.put("id", student.getId());
                    data.put("userId", student.getUser().getId());
                    data.put("name", student.getName());
                    data.put("email", student.getUser().getEmail());
                    data.put("phone", student.getPhone());
                    data.put("branch", student.getBranch());
                    data.put("graduationYear", student.getGraduationYear());
                    data.put("cgpa", student.getCgpa());
                    data.put("backlogs", student.getBacklogs());

                    // Student account status
                    boolean enabled = student.getUser().isEnabled();

                    data.put("enabled", enabled);
                    data.put("status", enabled ? "ACTIVE" : "DISABLED");

                    return data;
                })
                .toList();

        return ResponseEntity.ok(result);
    }

    // Get all recruiters
    @GetMapping("/recruiters")
    public ResponseEntity<List<Map<String, Object>>> getAllRecruiters() {

        List<Map<String, Object>> result = userRepository.findAll()
                .stream()
                .filter(user -> "RECRUITER".equals(
                        user.getRole().getName()))
                .map(user -> {

                    Map<String, Object> data = new HashMap<>();

                    data.put("id", user.getId());
                    data.put("email", user.getEmail());
                    data.put("enabled", user.isEnabled());

                    return data;
                })
                .toList();

        return ResponseEntity.ok(result);
    }

    // Get all applications
    @GetMapping("/applications")
    public ResponseEntity<List<Map<String, Object>>> getAllApplications() {

        List<Map<String, Object>> result =
                applicationRepository.findAll()
                        .stream()
                        .map(application -> {

                            Map<String, Object> data = new HashMap<>();

                            data.put("id", application.getId());
                            data.put("status", application.getStatus());
                            data.put("appliedAt", application.getAppliedAt());

                            Student student = application.getStudent();
                            PlacementDrive drive =
                                    application.getPlacementDrive();

                            data.put("studentId", student.getId());
                            data.put("studentName", student.getName());
                            data.put("studentEmail",
                                    student.getUser().getEmail());

                            data.put("companyName", drive.getCompanyName());
                            data.put("jobTitle", drive.getJobTitle());
                            data.put("driveId", drive.getId());

                            return data;
                        })
                        .toList();

        return ResponseEntity.ok(result);
    }
}
