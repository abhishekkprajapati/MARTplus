package com.ShopSphere.service;

import com.ShopSphere.entity.OtpVerification;
import com.ShopSphere.entity.User;
import com.ShopSphere.repository.OtpVerificationRepository;
import com.ShopSphere.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final DisposableEmailValidator disposableEmailValidator;
    private final EmailService emailService;
    private final SmsOtpService smsOtpService;
    private final OtpService otpService;
    private final OtpVerificationRepository otpVerificationRepository;

    public UserService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            DisposableEmailValidator disposableEmailValidator,
            EmailService emailService,
            SmsOtpService smsOtpService,
            OtpService otpService,
            OtpVerificationRepository otpVerificationRepository
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.disposableEmailValidator = disposableEmailValidator;
        this.emailService = emailService;
        this.smsOtpService = smsOtpService;
        this.otpService = otpService;
        this.otpVerificationRepository = otpVerificationRepository;
    }

    // =========================
    // PUBLIC USER REGISTRATION
    // =========================

    public User registerUser(User user) {

        // 1. Email format and disposable validation
        disposableEmailValidator.validateEmail(user.getEmail());
        String normalizedEmail = user.getEmail().trim().toLowerCase();
        user.setEmail(normalizedEmail);

        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new RuntimeException("Email already registered");
        }

        // 2. Phone normalization and validation
        if (user.getPhone() == null || user.getPhone().isBlank()) {
            throw new RuntimeException("Phone number is required");
        }
        String normalizedPhone = smsOtpService.normalizeIndianPhone(user.getPhone());
        user.setPhone(normalizedPhone);

        if (userRepository.existsByPhone(normalizedPhone)) {
            throw new RuntimeException("Phone number already registered");
        }

        if (user.getPassword() == null || user.getPassword().isBlank()) {
            throw new RuntimeException("Password is required");
        }

        user.setPassword(
                passwordEncoder.encode(user.getPassword())
        );

        // Public registration strictly creates BUYER accounts only
        user.setRole("BUYER");

        // Standard direct registration without OTP blocking
        user.setEmailVerified(true);
        user.setPhoneVerified(true);
        user.setStatus("ACTIVE");

        return userRepository.save(user);
    }

    // =========================
    // SEND PRE-REGISTRATION PHONE OTP
    // =========================

    public String sendPhoneOtp(String rawPhone) {
        if (rawPhone == null || rawPhone.isBlank()) {
            throw new RuntimeException("Phone number is required.");
        }
        String normalizedPhone = smsOtpService.normalizeIndianPhone(rawPhone);

        if (userRepository.existsByPhone(normalizedPhone)) {
            throw new RuntimeException("Phone number already registered. Please log in or use a different number.");
        }

        String otp = otpService.generate6DigitOtp();
        otpService.createOtp(null, normalizedPhone, "PHONE_VERIFICATION", otp);
        smsOtpService.sendSmsOtp(normalizedPhone, otp, "PHONE_VERIFICATION");
        return normalizedPhone;
    }

    // =========================
    // VERIFY EMAIL
    // =========================

    public User verifyEmail(String email, String otp) {
        if (email == null || email.isBlank()) {
            throw new RuntimeException("Email is required.");
        }
        String normalizedEmail = email.trim().toLowerCase();
        User user = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + normalizedEmail));

        otpService.verifyOtp(normalizedEmail, "EMAIL_VERIFICATION", otp);

        user.setEmailVerified(true);
        if (Boolean.TRUE.equals(user.getPhoneVerified())) {
            user.setStatus("ACTIVE");
        }
        return userRepository.save(user);
    }

    // =========================
    // VERIFY PHONE (Supports both pre-reg & post-reg)
    // =========================

    public Map<String, Object> verifyPhone(String phone, String otp) {
        if (phone == null || phone.isBlank()) {
            throw new RuntimeException("Phone number is required.");
        }
        String normalizedPhone = smsOtpService.normalizeIndianPhone(phone);

        otpService.verifyOtp(normalizedPhone, "PHONE_VERIFICATION", otp);

        boolean emailVerified = false;
        String status = "PENDING_VERIFICATION";
        Optional<User> userOpt = userRepository.findByPhone(normalizedPhone);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setPhoneVerified(true);
            if (Boolean.TRUE.equals(user.getEmailVerified())) {
                user.setStatus("ACTIVE");
            }
            userRepository.save(user);
            emailVerified = Boolean.TRUE.equals(user.getEmailVerified());
            status = user.getStatus();
        }

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("phoneVerified", true);
        result.put("phone", normalizedPhone);
        result.put("emailVerified", emailVerified);
        result.put("status", status);
        result.put("message", "Mobile number verified successfully.");
        return result;
    }

    // =========================
    // RESEND EMAIL OTP
    // =========================

    public void resendEmailOtp(String email) {
        if (email == null || email.isBlank()) {
            throw new RuntimeException("Email is required.");
        }
        String normalizedEmail = email.trim().toLowerCase();
        User user = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + normalizedEmail));

        if (Boolean.TRUE.equals(user.getEmailVerified())) {
            throw new RuntimeException("Email is already verified.");
        }

        String emailOtp = otpService.generate6DigitOtp();
        otpService.createOtp(user.getId(), normalizedEmail, "EMAIL_VERIFICATION", emailOtp);
        emailService.sendEmailVerificationOtp(normalizedEmail, user.getFirstName(), emailOtp);
    }

    // =========================
    // RESEND PHONE OTP (Supports both pre-reg & post-reg)
    // =========================

    public void resendPhoneOtp(String phoneOrEmail) {
        if (phoneOrEmail == null || phoneOrEmail.isBlank()) {
            throw new RuntimeException("Phone or email is required.");
        }
        String clean = phoneOrEmail.trim();
        String normalizedTarget;
        Long userId = null;

        if (clean.contains("@")) {
            User user = userRepository.findByEmail(clean.toLowerCase())
                    .orElseThrow(() -> new RuntimeException("User not found with email: " + clean));
            if (Boolean.TRUE.equals(user.getPhoneVerified())) {
                throw new RuntimeException("Mobile number is already verified.");
            }
            normalizedTarget = user.getPhone();
            userId = user.getId();
        } else {
            normalizedTarget = smsOtpService.normalizeIndianPhone(clean);
            Optional<User> userOpt = userRepository.findByPhone(normalizedTarget);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                if (Boolean.TRUE.equals(user.getPhoneVerified())) {
                    throw new RuntimeException("Mobile number is already verified.");
                }
                userId = user.getId();
            }
        }

        String phoneOtp = otpService.generate6DigitOtp();
        otpService.createOtp(userId, normalizedTarget, "PHONE_VERIFICATION", phoneOtp);
        smsOtpService.sendSmsOtp(normalizedTarget, phoneOtp, "PHONE_VERIFICATION");
    }

    // =========================
    // ADMIN CREATE USER
    // =========================

    public User createAdminManagedUser(User user) {

        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("Email already registered");
        }

        if (user.getPhone() != null
                && !user.getPhone().isBlank()
                && userRepository.existsByPhone(user.getPhone())) {

            throw new RuntimeException("Phone number already registered");
        }

        user.setPassword(
                passwordEncoder.encode(user.getPassword())
        );

        if (user.getStatus() == null
                || user.getStatus().isBlank()) {

            user.setStatus("ACTIVE");
        }

        return userRepository.save(user);
    }

    // =========================
    // GET USER BY EMAIL
    // =========================

    public User getUserByEmail(String email) {

        return userRepository.findByEmail(email)
                .orElseThrow(
                        () -> new RuntimeException("User not found")
                );
    }

    // =========================
    // GET USER BY ID
    // =========================

    public User getUserById(Long id) {

        return userRepository.findById(id)
                .orElseThrow(
                        () -> new RuntimeException("User not found")
                );
    }

    // =========================
    // ADMIN UPDATE USER
    // =========================

    public User updateUser(
            Long id,
            User updatedUser
    ) {

        User existingUser = userRepository.findById(id)
                .orElseThrow(
                        () -> new RuntimeException("User not found")
                );

        // Email
        if (updatedUser.getEmail() != null
                && !updatedUser.getEmail()
                .equalsIgnoreCase(existingUser.getEmail())) {

            if (userRepository.existsByEmail(
                    updatedUser.getEmail())) {

                throw new RuntimeException(
                        "Email already registered"
                );
            }

            existingUser.setEmail(
                    updatedUser.getEmail()
            );
        }

        // Phone
        if (updatedUser.getPhone() != null
                && !updatedUser.getPhone()
                .equals(existingUser.getPhone())) {

            if (userRepository.existsByPhone(
                    updatedUser.getPhone())) {

                throw new RuntimeException(
                        "Phone number already registered"
                );
            }

            existingUser.setPhone(
                    updatedUser.getPhone()
            );
        }

        // First name
        if (updatedUser.getFirstName() != null) {

            existingUser.setFirstName(
                    updatedUser.getFirstName()
            );
        }

        // Last name
        if (updatedUser.getLastName() != null) {

            existingUser.setLastName(
                    updatedUser.getLastName()
            );
        }

        // Role
        if (updatedUser.getRole() != null) {

            String role =
                    updatedUser.getRole().toUpperCase();

            if (!role.equals("BUYER")
                    && !role.equals("SELLER")) {

                throw new RuntimeException(
                        "Only BUYER or SELLER roles are allowed"
                );
            }

            existingUser.setRole(role);
        }

        // Status
        if (updatedUser.getStatus() != null) {

            String status =
                    updatedUser.getStatus().toUpperCase();

            if (!status.equals("ACTIVE")
                    && !status.equals("INACTIVE")) {

                throw new RuntimeException(
                        "Status must be ACTIVE or INACTIVE"
                );
            }

            existingUser.setStatus(status);
        }

        // Password
        if (updatedUser.getPassword() != null
                && !updatedUser.getPassword().isBlank()) {

            existingUser.setPassword(
                    passwordEncoder.encode(
                            updatedUser.getPassword()
                    )
            );
        }

        return userRepository.save(existingUser);
    }

    // =========================
    // GET ALL USERS
    // =========================

    public List<User> getAllUsers() {

        return userRepository.findAll();
    }

    // =========================
    // DEACTIVATE USER
    // =========================

    public User deactivateUser(Long id) {

        User user = userRepository.findById(id)
                .orElseThrow(
                        () -> new RuntimeException("User not found")
                );

        if ("ADMIN".equalsIgnoreCase(user.getRole())) {

            throw new RuntimeException(
                    "Admin user cannot be deactivated"
            );
        }

        user.setStatus("INACTIVE");

        return userRepository.save(user);
    }

    // =========================
    // BUYER PROFILE UPDATE
    // =========================

    public User updateBuyerProfile(
            String email,
            String firstName,
            String lastName,
            String phone
    ) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(
                        () -> new RuntimeException("User not found")
                );

        // First name
        if (firstName != null
                && !firstName.isBlank()) {

            user.setFirstName(firstName);
        }

        // Last name
        if (lastName != null
                && !lastName.isBlank()) {

            user.setLastName(lastName);
        }

        // Phone
        if (phone != null
                && !phone.isBlank()
                && !phone.equals(user.getPhone())) {

            if (userRepository.existsByPhone(phone)) {

                throw new RuntimeException(
                        "Phone number already registered"
                );
            }

            user.setPhone(phone);
        }

        return userRepository.save(user);
    }

    // =========================
    // CHANGE PASSWORD
    // =========================

    public User changePassword(
            String email,
            String currentPassword,
            String newPassword
    ) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(
                        () -> new RuntimeException("User not found")
                );

        // Current password check
        if (currentPassword == null
                || currentPassword.isBlank()) {

            throw new RuntimeException(
                    "Current password is required"
            );
        }

        // New password check
        if (newPassword == null
                || newPassword.isBlank()) {

            throw new RuntimeException(
                    "New password is required"
            );
        }

        // Minimum password length
        if (newPassword.length() < 8) {

            throw new RuntimeException(
                    "New password must be at least 8 characters"
            );
        }

        // Verify current password
        if (!passwordEncoder.matches(
                currentPassword,
                user.getPassword()
        )) {

            throw new RuntimeException(
                    "Current password is incorrect"
            );
        }

        // Prevent same password
        if (passwordEncoder.matches(
                newPassword,
                user.getPassword()
        )) {

            throw new RuntimeException(
                    "New password must be different from current password"
            );
        }

        // Hash new password
        user.setPassword(
                passwordEncoder.encode(newPassword)
        );

        return userRepository.save(user);
    }

    // =========================
    // FORGOT PASSWORD
    // =========================

    public void forgotPassword(String email) {
        if (email == null || email.isBlank()) {
            throw new RuntimeException("Please enter your email address.");
        }
        String cleanEmail = email.trim().toLowerCase();
        if (!cleanEmail.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            throw new RuntimeException("Please enter a valid email address.");
        }

        Optional<User> userOpt = userRepository.findByEmail(cleanEmail);
        if (userOpt.isEmpty()) {
            // Account Enumeration Protection: silently return without revealing account status
            return;
        }

        User user = userOpt.get();
        String rawOtp = otpService.generate6DigitOtp();

        // createOtp enforces cooldown (60s) and max requests per hour (5)
        otpService.createOtp(user.getId(), cleanEmail, "PASSWORD_RESET", rawOtp);

        try {
            emailService.sendPasswordResetOtp(cleanEmail, user.getFirstName(), rawOtp);
        } catch (Exception e) {
            System.err.println("Warning: Could not send password reset email: " + e.getMessage());
        }
    }

    // =========================
    // VERIFY PASSWORD RESET CODE
    // =========================

    public Map<String, Object> verifyPasswordReset(String email, String otp) {
        if (email == null || email.isBlank()) {
            throw new RuntimeException("Please enter your email address.");
        }
        String cleanEmail = email.trim().toLowerCase();
        String resetToken = otpService.verifyPasswordResetOtp(cleanEmail, otp);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("resetToken", resetToken);
        response.put("message", "Verification code verified successfully.");
        return response;
    }

    // =========================
    // RESET PASSWORD
    // =========================

    public void resetPassword(String email, String resetToken, String newPassword, String confirmPassword) {
        if (email == null || email.isBlank()) {
            throw new RuntimeException("Please enter your email address.");
        }
        if (resetToken == null || resetToken.isBlank()) {
            throw new RuntimeException("Invalid or missing password reset authorization.");
        }
        if (newPassword == null || newPassword.isBlank()) {
            throw new RuntimeException("Please enter a new password.");
        }
        if (newPassword.length() < 8) {
            throw new RuntimeException("Password must be at least 8 characters long.");
        }
        if (!newPassword.equals(confirmPassword)) {
            throw new RuntimeException("Passwords do not match.");
        }

        String cleanEmail = email.trim().toLowerCase();

        // Verify reset challenge in repository
        OtpVerification record = otpVerificationRepository
                .findByChallengeIdAndPurpose(resetToken.trim(), "PASSWORD_RESET")
                .orElseThrow(() -> new RuntimeException("Invalid or expired password reset authorization."));

        if (!Boolean.TRUE.equals(record.getVerified())) {
            throw new RuntimeException("Verification code has not been verified.");
        }

        if (!cleanEmail.equalsIgnoreCase(record.getTarget())) {
            throw new RuntimeException("Reset authorization does not match the provided email.");
        }

        // Authorization valid for up to 15 minutes after issuance
        LocalDateTime now = LocalDateTime.now();
        if (now.isAfter(record.getExpiresAt().plusMinutes(15))) {
            throw new RuntimeException("Password reset authorization has expired. Please request a new code.");
        }

        User user = userRepository.findByEmail(cleanEmail)
                .orElseThrow(() -> new RuntimeException("User not found."));

        // Single-use guarantee: Invalidate reset authorization token immediately
        record.setPurpose("PASSWORD_RESET_USED");
        record.setChallengeId(null);
        otpVerificationRepository.save(record);

        // Hash new password using BCrypt PasswordEncoder
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setEmailVerified(true);
        if (Boolean.TRUE.equals(user.getPhoneVerified())) {
            user.setStatus("ACTIVE");
        }
        userRepository.save(user);
    }

    // =========================
    // RESEND PASSWORD RESET
    // =========================

    public void resendPasswordReset(String email) {
        forgotPassword(email);
    }
}