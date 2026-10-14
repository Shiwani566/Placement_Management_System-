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
    public SecurityFilterChain securityFilterChain(HttpSecurity http)
            throws Exception {

        http
                .csrf(csrf -> csrf.disable())

                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )

                .authorizeHttpRequests(auth -> auth

                        // Public authentication endpoints
                        .requestMatchers(
                                "/api/auth/register",
                                "/api/auth/login"
                        ).permitAll()

                        // Student profile permissions
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/students"
                        ).hasRole("ADMIN")

                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/students/**"
                        ).hasAnyRole("STUDENT", "ADMIN")

                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/students"
                        ).hasAnyRole("STUDENT", "ADMIN")

                        .requestMatchers(
                                HttpMethod.DELETE,
                                "/api/students/**"
                        ).hasRole("ADMIN")

                        // Education permissions
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/education"
                        ).hasRole("ADMIN")

                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/education/**"
                        ).hasAnyRole("STUDENT", "ADMIN")

                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/education"
                        ).hasAnyRole("STUDENT", "ADMIN")

                        .requestMatchers(
                                HttpMethod.DELETE,
                                "/api/education/**"
                        ).hasAnyRole("STUDENT", "ADMIN")

                        // Skill permissions
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/skills",
                                "/api/skills/**"
                        ).hasAnyRole("STUDENT", "RECRUITER", "ADMIN")

                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/skills"
                        ).hasRole("ADMIN")

                        .requestMatchers(
                                HttpMethod.PUT,
                                "/api/skills/**"
                        ).hasRole("ADMIN")

                        .requestMatchers(
                                HttpMethod.DELETE,
                                "/api/skills/**"
                        ).hasRole("ADMIN")

                        // Student skill permissions
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/student-skills",
                                "/api/student-skills/**"
                        ).hasAnyRole("STUDENT", "RECRUITER", "ADMIN")

                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/student-skills"
                        ).hasAnyRole("STUDENT", "ADMIN")

                        .requestMatchers(
                                HttpMethod.DELETE,
                                "/api/student-skills/**"
                        ).hasAnyRole("STUDENT", "ADMIN")

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

