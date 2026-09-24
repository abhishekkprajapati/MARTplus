package com.ShopSphere.service;

import com.ShopSphere.entity.OtpVerification;
import com.ShopSphere.repository.OtpVerificationRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class OtpService {

    private final OtpVerificationRepository otpRepository;
    private final PasswordEncoder passwordEncoder;
    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${otp.expiry.minutes:10}")
    private int otpExpiryMinutes;

    @Value("${otp.max.attempts:5}")
    private int maxAttempts;

    @Value("${otp.resend.cooldown.seconds:60}")
    private int resendCooldownSeconds;

    @Value("${otp.dev-mode:true}")
    private boolean devMode;

    // In-memory cache for dev testing when devMode=true (Never exposed in production)
    private final Map<String, String> devOtpCache = new ConcurrentHashMap<>();

    public OtpService(
            OtpVerificationRepository otpRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.otpRepository = otpRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Generates a cryptographically secure 6-digit OTP.
     */
    public String generate6DigitOtp() {
        int code = 100000 + secureRandom.nextInt(900000);
        return String.valueOf(code);
    }

    /**
     * Creates, hashes, and stores a new OTP verification record.
     */
    @Transactional
    public OtpVerification createOtp(Long userId, String target, String purpose, String rawOtp) {
        String cleanTarget = target.trim().toLowerCase();
        LocalDateTime now = LocalDateTime.now();

        // 1. Rate Limiting: Check cooldown (60 seconds)
        otpRepository.findTopByTargetAndPurposeOrderByIdDesc(cleanTarget, purpose)
                .ifPresent(existing -> {
                    if (existing.getLastSentAt() != null) {
                        long elapsedSeconds = Duration.between(existing.getLastSentAt(), now).getSeconds();
                        if (elapsedSeconds < resendCooldownSeconds) {
                            throw new RuntimeException("Please wait before requesting another OTP.");
                        }
                    }
                });

        // 2. Rate Limiting: Max 5 OTP requests per hour per target
        long requestsPastHour = otpRepository.countByTargetAndCreatedAtAfter(cleanTarget, now.minusHours(1));
        if (requestsPastHour >= 5) {
            throw new RuntimeException("Too many OTP requests. Please wait a while before requesting again.");
        }

        // 3. Hash OTP before persisting to PostgreSQL
        String otpHash = passwordEncoder.encode(rawOtp);

        OtpVerification record = new OtpVerification();
        record.setUserId(userId);
        record.setTarget(cleanTarget);
        record.setPurpose(purpose);
        record.setOtpHash(otpHash);
        record.setExpiresAt(now.plusMinutes(otpExpiryMinutes));
        record.setAttempts(0);
        record.setVerified(false);
        record.setCreatedAt(now);
        record.setLastSentAt(now);

        if ("LOGIN".equalsIgnoreCase(purpose)) {
            record.setChallengeId(UUID.randomUUID().toString());
        }

        OtpVerification saved = otpRepository.save(record);

        if (devMode) {
            String normalizedKey = (cleanTarget + ":" + purpose).toLowerCase();
            devOtpCache.put(normalizedKey, rawOtp);
            devOtpCache.put(cleanTarget, rawOtp);
            if (saved.getChallengeId() != null) {
                devOtpCache.put(saved.getChallengeId().toLowerCase(), rawOtp);
            }
        }

        return saved;
    }

    /**
     * Verifies OTP against target and purpose (for Email or Mobile verification).
     */
    @Transactional
    public boolean verifyOtp(String target, String purpose, String rawOtp) {
        if (rawOtp == null || rawOtp.trim().length() != 6) {
            throw new RuntimeException("Invalid OTP.");
        }

        String cleanTarget = target.trim().toLowerCase();
        LocalDateTime now = LocalDateTime.now();

        OtpVerification record = otpRepository.findTopByTargetAndPurposeOrderByIdDesc(cleanTarget, purpose)
                .orElseThrow(() -> new RuntimeException("Invalid OTP."));

        if (Boolean.TRUE.equals(record.getVerified())) {
            throw new RuntimeException("OTP has already been used.");
        }

        if (now.isAfter(record.getExpiresAt())) {
            throw new RuntimeException("OTP has expired. Please request a new OTP.");
        }

        if (record.getAttempts() >= maxAttempts) {
            throw new RuntimeException("Too many incorrect attempts. Please request a new OTP.");
        }

        if (!passwordEncoder.matches(rawOtp.trim(), record.getOtpHash())) {
            record.setAttempts(record.getAttempts() + 1);
            otpRepository.save(record);

            if (record.getAttempts() >= maxAttempts) {
                throw new RuntimeException("Too many incorrect attempts. Please request a new OTP.");
            }
            throw new RuntimeException("Invalid OTP.");
        }

        // Successfully verified
        record.setVerified(true);
        otpRepository.save(record);
        return true;
    }

    /**
     * Verifies Password Reset OTP.
     * On successful verification, generates a single-use short-lived resetToken challenge and associates it with the record.
     */
    @Transactional(noRollbackFor = RuntimeException.class)
    public String verifyPasswordResetOtp(String targetEmail, String rawOtp) {
        if (targetEmail == null || targetEmail.isBlank()) {
            throw new RuntimeException("Email is required.");
        }
        if (rawOtp == null || rawOtp.trim().length() != 6) {
            throw new RuntimeException("Invalid verification code.");
        }

        String cleanTarget = targetEmail.trim().toLowerCase();
        LocalDateTime now = LocalDateTime.now();

        OtpVerification record = otpRepository.findTopByTargetAndPurposeOrderByIdDesc(cleanTarget, "PASSWORD_RESET")
                .orElseThrow(() -> new RuntimeException("Invalid verification code."));

        if (Boolean.TRUE.equals(record.getVerified())) {
            throw new RuntimeException("Verification code has already been used. Please request a new code.");
        }

        if (now.isAfter(record.getExpiresAt())) {
            throw new RuntimeException("Verification code has expired. Please request a new code.");
        }

        if (record.getAttempts() >= maxAttempts) {
            throw new RuntimeException("Too many incorrect attempts. Please request a new code.");
        }

        if (!passwordEncoder.matches(rawOtp.trim(), record.getOtpHash())) {
            record.setAttempts(record.getAttempts() + 1);
            otpRepository.saveAndFlush(record);

            if (record.getAttempts() >= maxAttempts) {
                throw new RuntimeException("Too many incorrect attempts. Please request a new code.");
            }
            throw new RuntimeException("Invalid verification code.");
        }

        // Successfully verified: issue a short-lived reset authorization challenge (resetToken)
        String resetToken = UUID.randomUUID().toString();
        record.setVerified(true);
        record.setChallengeId(resetToken);
        otpRepository.save(record);

        if (devMode) {
            devOtpCache.put(resetToken.toLowerCase(), "RESET_TOKEN_VERIFIED");
        }

        return resetToken;
    }

    /**
     * Verifies Login OTP using temporary challengeId.
     */
    @Transactional
    public OtpVerification verifyLoginOtp(String challengeId, String rawOtp) {
        if (challengeId == null || challengeId.isBlank() || rawOtp == null || rawOtp.trim().length() != 6) {
            throw new RuntimeException("Invalid login verification code.");
        }

        LocalDateTime now = LocalDateTime.now();

        OtpVerification record = otpRepository.findByChallengeIdAndPurpose(challengeId.trim(), "LOGIN")
                .orElseThrow(() -> new RuntimeException("Invalid login challenge session."));

        if (Boolean.TRUE.equals(record.getVerified())) {
            throw new RuntimeException("Login code has already been verified.");
        }

        if (now.isAfter(record.getExpiresAt())) {
            throw new RuntimeException("OTP has expired. Please request a new OTP.");
        }

        if (record.getAttempts() >= maxAttempts) {
            throw new RuntimeException("Too many incorrect attempts. Please request a new OTP.");
        }

        if (!passwordEncoder.matches(rawOtp.trim(), record.getOtpHash())) {
            record.setAttempts(record.getAttempts() + 1);
            otpRepository.save(record);

            if (record.getAttempts() >= maxAttempts) {
                throw new RuntimeException("Too many incorrect attempts. Please request a new OTP.");
            }
            throw new RuntimeException("Invalid login verification code.");
        }

        record.setVerified(true);
        return otpRepository.save(record);
    }

    /**
     * Strictly dev-mode inspection for local tests.
     */
    public String getDevOtp(String key) {
        if (!devMode) return null;
        return devOtpCache.get(key != null ? key.trim().toLowerCase() : "");
    }
}
