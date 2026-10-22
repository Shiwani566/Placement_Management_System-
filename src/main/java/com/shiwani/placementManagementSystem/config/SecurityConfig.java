package com.shiwani.placementManagementSystem.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf.disable())

                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                .authorizeHttpRequests(auth -> auth

                        // Public authentication endpoints
                        .requestMatchers(
                                "/api/auth/login",
                                "/api/auth/register"
                        ).permitAll()

                        // Allow Spring Boot error responses
                        .requestMatchers("/error").permitAll()

                        // Placement drives: all authenticated roles can view
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/placement-drives",
                                "/api/placement-drives/**"
                        ).hasAnyRole("STUDENT", "RECRUITER", "ADMIN")

                        // Only ADMIN can create placement drives
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/placement-drives",
                                "/api/placement-drives/**"
                        ).hasRole("ADMIN")

                        // Only ADMIN can update placement drives
                        .requestMatchers(
                                HttpMethod.PUT,
                                "/api/placement-drives/**"
                        ).hasRole("ADMIN")

                        // Only ADMIN can delete placement drives
                        .requestMatchers(
                                HttpMethod.DELETE,
                                "/api/placement-drives/**"
                        ).hasRole("ADMIN")

                        // Students can apply for placement drives
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/placement-applications/apply/**"
                        ).hasRole("STUDENT")

                        // Students can view their own applications
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/placement-applications/my-applications"
                        ).hasRole("STUDENT")

                        // ADMIN can view applications for a drive
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/placement-applications/drive/**"
                        ).hasRole("ADMIN")

                        // ADMIN can update application status
                        .requestMatchers(
                                HttpMethod.PATCH,
                                "/api/placement-applications/**"
                        ).hasRole("ADMIN")

                        // View all student profiles: ADMIN and RECRUITER
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/students"
                        ).hasAnyRole("ADMIN", "RECRUITER")

                        // View individual student profiles
                        // Controller enforces student ownership
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/students/**"
                        ).hasAnyRole("STUDENT", "RECRUITER", "ADMIN")

                        // Create student profiles: STUDENT and ADMIN
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/students"
                        ).hasAnyRole("STUDENT", "ADMIN")

                        // Delete student profiles: ADMIN only
                        .requestMatchers(
                                HttpMethod.DELETE,
                                "/api/students/**"
                        ).hasRole("ADMIN")

                        // All other endpoints require authentication
                        .anyRequest().authenticated()
                )

                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }
}

