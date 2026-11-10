package com.shiwani.placementManagementSystem.config;

import com.shiwani.placementManagementSystem.config.JwtAuthenticationFilter;

import jakarta.servlet.DispatcherType;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;

@Configuration
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(
            JwtAuthenticationFilter jwtAuthenticationFilter) {

        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    // Password encryption
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Main security configuration
    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http) throws Exception {

        http

                // Disable CSRF for REST APIs
                .csrf(csrf -> csrf.disable())

                // Stateless JWT authentication
                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )

                // Return 401 when authentication is missing
                .exceptionHandling(exception ->
                        exception.authenticationEntryPoint(
                                new HttpStatusEntryPoint(
                                        HttpStatus.UNAUTHORIZED
                                )
                        )
                )

                // Authorization rules
                .authorizeHttpRequests(auth -> auth

                        // Allow ERROR dispatch
                        .dispatcherTypeMatchers(
                                DispatcherType.ERROR
                        ).permitAll()

                        // ---------------------------------
                        // PUBLIC FRONTEND PAGES
                        // ---------------------------------

                        .requestMatchers(
                                "/",
                                "/index.html",
                                "/register.html",
                                "/favicon.ico",
                                "/student-dashboard.html",
                                "/recruiter-dashboard.html",
                                "/admin-dashboard.html",
                                "/css/**",
                                "/js/**",
                                "/images/**",
                                "/assets/**"
                        ).permitAll()

                        // ---------------------------------
                        // PUBLIC AUTH APIs
                        // ---------------------------------

                        .requestMatchers(
                                "/api/auth/login",
                                "/api/auth/register"
                        ).permitAll()

                        // ---------------------------------
                        // ADMIN APIs
                        // ---------------------------------

                        .requestMatchers("/api/admin/**")
                        .hasRole("ADMIN")

                        // ---------------------------------
                        // STUDENT APIs
                        // ---------------------------------

                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/students"
                        ).hasAnyRole("ADMIN", "RECRUITER")

                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/students/**"
                        ).hasAnyRole(
                                "STUDENT",
                                "RECRUITER",
                                "ADMIN"
                        )

                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/students"
                        ).hasAnyRole("STUDENT", "ADMIN")

                        .requestMatchers(
                                HttpMethod.PUT,
                                "/api/students/**"
                        ).hasAnyRole("STUDENT", "ADMIN")

                        .requestMatchers(
                                HttpMethod.DELETE,
                                "/api/students/**"
                        ).hasRole("ADMIN")

                        // ---------------------------------
                        // EDUCATION APIs
                        // ---------------------------------

                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/education/**"
                        ).hasAnyRole("STUDENT", "ADMIN")

                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/education/**"
                        ).hasAnyRole("STUDENT", "ADMIN")

                        .requestMatchers(
                                HttpMethod.DELETE,
                                "/api/education/**"
                        ).hasAnyRole("STUDENT", "ADMIN")

                        // ---------------------------------
                        // SKILLS APIs
                        // ---------------------------------

                        .requestMatchers(
                                "/api/skills/**",
                                "/api/student-skills/**"
                        ).hasAnyRole(
                                "STUDENT",
                                "RECRUITER",
                                "ADMIN"
                        )

                        // ---------------------------------
                        // PLACEMENT DRIVE APIs
                        // ---------------------------------

                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/placement-drives",
                                "/api/placement-drives/**"
                        ).hasAnyRole(
                                "STUDENT",
                                "RECRUITER",
                                "ADMIN"
                        )

                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/placement-drives/**"
                        ).hasAnyRole("RECRUITER", "ADMIN")

                        .requestMatchers(
                                HttpMethod.PUT,
                                "/api/placement-drives/**"
                        ).hasAnyRole("RECRUITER", "ADMIN")

                        .requestMatchers(
                                HttpMethod.DELETE,
                                "/api/placement-drives/**"
                        ).hasAnyRole("RECRUITER", "ADMIN")

                        // ---------------------------------
                        // PLACEMENT APPLICATION APIs
                        // ---------------------------------

                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/placement-applications/apply/**"
                        ).hasRole("STUDENT")

                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/placement-applications/my-applications"
                        ).hasRole("STUDENT")

                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/placement-applications/drive/**"
                        ).hasAnyRole("RECRUITER", "ADMIN")

                        .requestMatchers(
                                HttpMethod.PATCH,
                                "/api/placement-applications/**"
                        ).hasAnyRole("RECRUITER", "ADMIN")

                        // ---------------------------------
                        // ALL OTHER REQUESTS
                        // ---------------------------------

                        .anyRequest().authenticated()
                );

        // JWT filter before username/password authentication
        http.addFilterBefore(
                jwtAuthenticationFilter,
                UsernamePasswordAuthenticationFilter.class
        );

        return http.build();
    }
}
