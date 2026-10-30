package com.shiwani.placementManagementSystem.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
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

                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(
                                new HttpStatusEntryPoint(
                                        HttpStatus.UNAUTHORIZED
                                )
                        )
                )

                .authorizeHttpRequests(auth -> auth

                        // Authentication endpoints
                        .requestMatchers(
                                "/api/auth/login",
                                "/api/auth/register"
                        ).permitAll()

                        .requestMatchers("/error").permitAll()

                        // Placement drives: view drives
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

                        // Placement drives: create
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/placement-drives",
                                "/api/placement-drives/**"
                        ).hasAnyRole(
                                "RECRUITER",
                                "ADMIN"
                        )

                        // Placement drives: update
                        .requestMatchers(
                                HttpMethod.PUT,
                                "/api/placement-drives/**"
                        ).hasRole("ADMIN")

                        // Placement drives: delete
                        .requestMatchers(
                                HttpMethod.DELETE,
                                "/api/placement-drives/**"
                        ).hasAnyRole(
                                "RECRUITER",
                                "ADMIN"
                        )

                        // Placement applications: apply for a drive
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/placement-applications/apply/**"
                        ).hasRole("STUDENT")

                        // Placement applications: student's applications
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/placement-applications/my-applications"
                        ).hasRole("STUDENT")

                        // Placement applications: view drive applications
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/placement-applications/drive/**"
                        ).hasRole("ADMIN")

                        // Placement applications: update status
                        .requestMatchers(
                                HttpMethod.PATCH,
                                "/api/placement-applications/**"
                        ).hasRole("ADMIN")

                        // Student APIs: list all student profiles
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/students"
                        ).hasAnyRole(
                                "ADMIN",
                                "RECRUITER"
                        )

                        // Student APIs: individual profiles
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/students/**"
                        ).hasAnyRole(
                                "STUDENT",
                                "RECRUITER",
                                "ADMIN"
                        )

                        // Student APIs: create profile
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/students"
                        ).hasAnyRole(
                                "STUDENT",
                                "ADMIN"
                        )

                        // Student APIs: delete profile
                        .requestMatchers(
                                HttpMethod.DELETE,
                                "/api/students/**"
                        ).hasRole("ADMIN")

                        // Education APIs: view all records (ADMIN only)
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/education"
                        ).hasRole("ADMIN")

                        // Education APIs: individual/student records
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/education/**"
                        ).hasAnyRole(
                                "STUDENT",
                                "ADMIN"
                        )

                        // Education APIs: add education
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/education"
                        ).hasAnyRole(
                                "STUDENT",
                                "ADMIN"
                        )

                        // Education APIs: delete education
                        .requestMatchers(
                                HttpMethod.DELETE,
                                "/api/education/**"
                        ).hasAnyRole(
                                "STUDENT",
                                "ADMIN"
                        )

                        // Skills: all roles can view available skills
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/skills",
                                "/api/skills/**"
                        ).hasAnyRole(
                                "STUDENT",
                                "RECRUITER",
                                "ADMIN"
                        )

                        // Skills: only ADMIN can create
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/skills",
                                "/api/skills/**"
                        ).hasRole("ADMIN")

                        // Skills: only ADMIN can delete
                        .requestMatchers(
                                HttpMethod.DELETE,
                                "/api/skills/**"
                        ).hasRole("ADMIN")

                        // StudentSkill: all mappings (ADMIN/RECRUITER)
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/student-skills"
                        ).hasAnyRole(
                                "ADMIN",
                                "RECRUITER"
                        )

                        // Specific mapping or student skills
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/student-skills/student/**",
                                "/api/student-skills/*"
                        ).hasAnyRole(
                                "STUDENT",
                                "RECRUITER",
                                "ADMIN"
                        )

                        // Find students by skill
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/student-skills/skill/**"
                        ).hasAnyRole(
                                "ADMIN",
                                "RECRUITER"
                        )

                        // Add student skills
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/student-skills"
                        ).hasAnyRole(
                                "STUDENT",
                                "ADMIN"
                        )

                        // Delete student skill mapping
                        .requestMatchers(
                                HttpMethod.DELETE,
                                "/api/student-skills/**"
                        ).hasAnyRole(
                                "STUDENT",
                                "ADMIN"
                        )

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

