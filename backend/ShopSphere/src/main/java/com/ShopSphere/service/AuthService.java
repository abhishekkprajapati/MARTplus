package com.ShopSphere.service;

import com.ShopSphere.dto.LoginChallengeResponse;
import com.ShopSphere.dto.LoginRequest;
import com.ShopSphere.dto.LoginResponse;
import com.ShopSphere.dto.VerifyLoginOtpRequest;
import com.ShopSphere.entity.OtpVerification;
import com.ShopSphere.entity.User;
import com.ShopSphere.repository.OtpVerificationRepository;
import com.ShopSphere.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final OtpService otpService;
    private final SmsOtpService smsOtpService;
    private final OtpVerificationRepository otpRepository;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            OtpService otpService,
            SmsOtpService smsOtpService,
            OtpVerificationRepository otpRepository
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.otpService = otpService;
        this.smsOtpService = smsOtpService;
        this.otpRepository = otpRepository;
    }

    /**
     * Authenticates user credentials.
     * ADMIN: returns LoginResponse with JWT directly.
     * BUYER / SELLER: enforces email/phone verification, dispatches Login SMS OTP, and returns LoginChallengeResponse.
     */
    @Transactional
    public Object login(LoginRequest request) {
        if (request.getEmail() == null || request.getEmail().isBlank()) {
            throw new RuntimeException("Please enter your email address.");
        }
        if (request.getPassword() == null || request.getPassword().isBlank()) {
            throw new RuntimeException("Please enter your password.");
        }

        String email = request.getEmail().trim().toLowerCase();

       String role = request.getRole();

if (role == null || role.isBlank()) {
    throw new RuntimeException("Please select an account type.");
}

role = role.trim().toUpperCase();

if (!role.equals("BUYER") && !role.equals("SELLER") && !role.equals("ADMIN")) {
    throw new RuntimeException("Invalid account type.");
}

User user = userRepository.findByEmailAndRole(email, role)
        .orElseThrow(() -> new RuntimeException("Invalid email, password or account type"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid email or password");
        }

        // If user was previously in PENDING_VERIFICATION, activate them
        if ("PENDING_VERIFICATION".equalsIgnoreCase(user.getStatus())) {
            user.setStatus("ACTIVE");
            user.setEmailVerified(true);
            user.setPhoneVerified(true);
            userRepository.save(user);
        }

        if (!"ACTIVE".equalsIgnoreCase(user.getStatus())) {
            throw new RuntimeException("User account is not active");
        }

        String token = jwtService.generateToken(
                user.getId(),
                user.getEmail(),
                user.getRole()
        );

        return new LoginResponse(
                token,
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getRole()
        );
    }

    /**
     * Verifies the Login SMS OTP challenge and issues the authenticated JWT.
     */
    @Transactional
    public LoginResponse verifyLoginOtp(VerifyLoginOtpRequest request) {
        if (request.getChallengeId() == null || request.getChallengeId().isBlank()) {
            throw new RuntimeException("Invalid login challenge session.");
        }

        OtpVerification record = otpService.verifyLoginOtp(request.getChallengeId(), request.getOtp());

        User user = userRepository.findById(record.getUserId())
                .orElseThrow(() -> new RuntimeException("User associated with this login challenge was not found."));

        if (!"ACTIVE".equalsIgnoreCase(user.getStatus())) {
            throw new RuntimeException("User account is not active");
        }

        String token = jwtService.generateToken(
                user.getId(),
                user.getEmail(),
                user.getRole()
        );

        return new LoginResponse(
                token,
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getRole()
        );
    }

    /**
     * Resends a login SMS OTP for an active challenge session.
     */
    @Transactional
    public LoginChallengeResponse resendLoginOtp(String challengeId) {
        if (challengeId == null || challengeId.isBlank()) {
            throw new RuntimeException("Invalid login challenge session.");
        }

        OtpVerification oldRecord = otpRepository.findByChallengeIdAndPurpose(challengeId.trim(), "LOGIN")
                .orElseThrow(() -> new RuntimeException("Login challenge session not found or expired."));

        User user = userRepository.findById(oldRecord.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found."));

        String rawOtp = otpService.generate6DigitOtp();
        OtpVerification newVerification = otpService.createOtp(user.getId(), user.getPhone(), "LOGIN", rawOtp);
        smsOtpService.sendSmsOtp(user.getPhone(), rawOtp, "LOGIN");

        return new LoginChallengeResponse(
                true,
                newVerification.getChallengeId(),
                smsOtpService.maskPhoneNumber(user.getPhone()),
                "A new verification code has been sent to your registered mobile number"
        );
    }
}
