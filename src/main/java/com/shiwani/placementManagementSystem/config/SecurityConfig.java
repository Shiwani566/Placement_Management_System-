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

    public SecurityConfig(
            JwtAuthenticationFilter jwtAuthenticationFilter) {
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
                        .sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )

                .authorizeHttpRequests(auth -> auth

                        // Authentication endpoints
                        .requestMatchers(
                                "/api/auth/login",
                                "/api/auth/register"
                        ).permitAll()

                        .requestMatchers("/error").permitAll()

                        // Placement drive: viewing drives
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/placement-drives",
                                "/api/placement-drives/my-drives",
                                "/api/placement-drives/**"
                        ).hasAnyRole(
                                "STUDENT",
                                "RECRUITER",
                                "ADMIN"
                        )

                        // Placement drive: create
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/placement-drives",
                                "/api/placement-drives/**"
                        ).hasAnyRole(
                                "RECRUITER",
                                "ADMIN"
                        )

                        // Placement drive: update (Admin only for now)
                        .requestMatchers(
                                HttpMethod.PUT,
                                "/api/placement-drives/**"
                        ).hasRole("ADMIN")

                        // Placement drive: delete
                        .requestMatchers(
                                HttpMethod.DELETE,
                                "/api/placement-drives/**"
                        ).hasAnyRole(
                                "RECRUITER",
                                "ADMIN"
                        )

                        // Placement applications: apply
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/placement-applications/apply/**"
                        ).hasRole("STUDENT")

                        // Placement applications: student's applications
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/placement-applications/my-applications"
                        ).hasRole("STUDENT")

                        // Placement applications: view applications for a drive
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/placement-applications/drive/**"
                        ).hasRole("ADMIN")

                        // Placement applications: update status
                        .requestMatchers(
                                HttpMethod.PATCH,
                                "/api/placement-applications/**"
                        ).hasRole("ADMIN")

                        // Student APIs: list students
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/students"
                        ).hasAnyRole(
                                "ADMIN",
                                "RECRUITER"
                        )

                        // Student APIs: individual student profiles
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/students/**"
                        ).hasAnyRole(
                                "STUDENT",
                                "RECRUITER",
                                "ADMIN"
                        )

                        // Student APIs: create student
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/students"
                        ).hasAnyRole(
                                "STUDENT",
                                "ADMIN"
                        )

                        // Student APIs: delete student
                        .requestMatchers(
                                HttpMethod.DELETE,
                                "/api/students/**"
                        ).hasRole("ADMIN")

                        // All other requests require authentication
                        .anyRequest().authenticated()
                )

                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }
}

