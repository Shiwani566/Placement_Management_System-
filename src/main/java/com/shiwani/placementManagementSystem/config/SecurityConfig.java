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

// IntelliJ mein apne existing JwtAuthenticationFilter ka sahi import add karna.

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

                        // Allow Spring Boot to return the original error response
                        .requestMatchers("/error").permitAll()

                        // Placement drives
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/placement-drives",
                                "/api/placement-drives/**"
                        ).hasAnyRole("STUDENT", "RECRUITER", "ADMIN")

                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/placement-drives",
                                "/api/placement-drives/**"
                        ).hasRole("ADMIN")

                        .requestMatchers(
                                HttpMethod.PUT,
                                "/api/placement-drives/**"
                        ).hasRole("ADMIN")

                        .requestMatchers(
                                HttpMethod.DELETE,
                                "/api/placement-drives/**"
                        ).hasRole("ADMIN")

                        // Placement applications: students can apply
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/placement-applications/apply/**"
                        ).hasRole("STUDENT")

                        // Students can view their own applications
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/placement-applications/my-applications"
                        ).hasRole("STUDENT")

                        // Admin can view applications for a drive
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/placement-applications/drive/**"
                        ).hasRole("ADMIN")

                        // Admin can update application status
                        .requestMatchers(
                                HttpMethod.PATCH,
                                "/api/placement-applications/**"
                        ).hasRole("ADMIN")

                        // Keep all other existing endpoints protected
                        .anyRequest().authenticated()
                )

                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }
}

