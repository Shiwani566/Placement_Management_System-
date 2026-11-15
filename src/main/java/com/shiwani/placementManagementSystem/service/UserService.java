package com.shiwani.placementManagementSystem.service;

import com.shiwani.placementManagementSystem.dto.LoginRequest;
import com.shiwani.placementManagementSystem.dto.LoginResponse;
import com.shiwani.placementManagementSystem.dto.RegisterRequest;
import com.shiwani.placementManagementSystem.entity.PasswordResetToken;
import com.shiwani.placementManagementSystem.entity.Role;
import com.shiwani.placementManagementSystem.entity.User;
import com.shiwani.placementManagementSystem.repository.PasswordResetTokenRepository;
import com.shiwani.placementManagementSystem.repository.RoleRepository;
import com.shiwani.placementManagementSystem.repository.UserRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;

@Service
public class UserService {

    private static final Logger log =
            LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final JavaMailSender mailSender;

    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${app.frontend.base-url:http://localhost:8080}")
    private String frontendBaseUrl;

    @Value("${spring.mail.username:}")
    private String mailUsername;

    public UserService(
            UserRepository userRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            PasswordResetTokenRepository passwordResetTokenRepository,
            JavaMailSender mailSender) {

        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.mailSender = mailSender;
    }

    // Student Registration
    public User registerStudent(RegisterRequest request) {

        String email = validateRegistration(request);

        Role studentRole = roleRepository.findByName("STUDENT")
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "STUDENT role was not found in the database"
                ));

        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(studentRole);

        return userRepository.save(user);
    }

    // Admin-controlled Recruiter Registration
    public User registerRecruiter(RegisterRequest request) {

        String email = validateRegistration(request);

        Role recruiterRole = roleRepository.findByName("RECRUITER")
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "RECRUITER role was not found in the database"
                ));

        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(recruiterRole);

        return userRepository.save(user);
    }

    // Common Registration Validation
    private String validateRegistration(RegisterRequest request) {

        if (request.getEmail() == null
                || request.getEmail().isBlank()
                || request.getPassword() == null
                || request.getPassword().isBlank()) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Email and password are required"
            );
        }

        String email = request.getEmail().trim().toLowerCase();

        if (!email.matches(
                "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Please enter a valid email"
            );
        }

        if (request.getPassword().length() < 8) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Password must contain at least 8 characters"
            );
        }

        if (userRepository.findByEmail(email).isPresent()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Email is already registered"
            );
        }

        return email;
    }

    // Login
    public LoginResponse login(LoginRequest request) {

        if (request.getEmail() == null
                || request.getEmail().isBlank()
                || request.getPassword() == null
                || request.getPassword().isBlank()) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Email and password are required"
            );
        }

        String email = request.getEmail().trim().toLowerCase();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Invalid email or password"
                ));

        if (!passwordEncoder.matches(
                request.getPassword(),
                user.getPassword())) {

            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Invalid email or password"
            );
        }

        if (!user.isEnabled()) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Your account has been disabled"
            );
        }

        String token = jwtService.generateToken(user.getEmail());

        return new LoginResponse(
                "Login successful",
                user.getId(),
                user.getEmail(),
                user.getRole().getName(),
                token
        );
    }

    // Forgot Password
    @Transactional
    public void forgotPassword(String email) {

        if (email == null || email.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Email is required"
            );
        }

        String normalizedEmail = email.trim().toLowerCase();

        User user = userRepository.findByEmail(normalizedEmail)
                .orElse(null);

        if (user == null || !user.isEnabled()) {
            return;
        }

        passwordResetTokenRepository.deleteByUser(user);

        byte[] randomBytes = new byte[32];
        secureRandom.nextBytes(randomBytes);

        String rawToken = Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(randomBytes);

        String tokenHash = hashToken(rawToken);

        PasswordResetToken resetToken = new PasswordResetToken(
                tokenHash,
                user,
                LocalDateTime.now().plusMinutes(15)
        );

        passwordResetTokenRepository.save(resetToken);

        String resetLink = frontendBaseUrl
                + "/reset-password.html?token="
                + rawToken;

        try {

            SimpleMailMessage message = new SimpleMailMessage();

            message.setFrom(mailUsername);
            message.setTo(user.getEmail());
            message.setSubject("PlaceMate - Password Reset");

            message.setText(
                    "Hello,\n\n"
                            + "We received a request to reset your PlaceMate password.\n\n"
                            + "Use the following link to reset your password:\n"
                            + resetLink
                            + "\n\nThis link expires in 15 minutes and can be used only once."
                            + "\n\nIf you did not request this, you can ignore this email."
            );

            mailSender.send(message);

            log.info("Password reset email sent successfully to {}", user.getEmail());

        } catch (RuntimeException exception) {

            log.error("Password reset email sending failed", exception);

            passwordResetTokenRepository.deleteByUser(user);

            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "Unable to send password reset email. Please try again later."
            );
        }
    }

    // Reset Password
    @Transactional
    public void resetPassword(String rawToken, String newPassword) {

        if (rawToken == null || rawToken.isBlank()
                || newPassword == null || newPassword.isBlank()) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Token and new password are required"
            );
        }

        if (newPassword.length() < 8) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Password must contain at least 8 characters"
            );
        }

        String tokenHash = hashToken(rawToken);

        PasswordResetToken resetToken =
                passwordResetTokenRepository.findByTokenHash(tokenHash)
                        .orElseThrow(() -> new ResponseStatusException(
                                HttpStatus.BAD_REQUEST,
                                "Invalid or expired reset link"
                        ));

        if (resetToken.getExpiryDate().isBefore(LocalDateTime.now())) {

            passwordResetTokenRepository.delete(resetToken);

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid or expired reset link"
            );
        }

        User user = resetToken.getUser();

        user.setPassword(passwordEncoder.encode(newPassword));

        userRepository.save(user);

        passwordResetTokenRepository.delete(resetToken);
    }

    // SHA-256 Token Hashing
    private String hashToken(String token) {

        try {

            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            byte[] hash = digest.digest(
                    token.getBytes(StandardCharsets.UTF_8)
            );

            return HexFormat.of().formatHex(hash);

        } catch (Exception exception) {

            throw new IllegalStateException(
                    "Unable to process reset token",
                    exception
            );
        }
    }
}
