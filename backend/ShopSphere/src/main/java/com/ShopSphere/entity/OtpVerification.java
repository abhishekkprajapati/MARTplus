package com.ShopSphere.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "otp_verifications", indexes = {
    @Index(name = "idx_otp_target_purpose", columnList = "target, purpose"),
    @Index(name = "idx_otp_challenge_id", columnList = "challengeId")
})
public class OtpVerification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(nullable = false)
    private String target; // Normalized email or phone

    @Column(nullable = false)
    private String purpose; // EMAIL_VERIFICATION, PHONE_VERIFICATION, LOGIN

    @Column(nullable = false, length = 255)
    private String otpHash; // BCrypt-hashed OTP

    @Column(length = 64)
    private String challengeId; // UUID for login verification

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @Column(nullable = false)
    private Integer attempts = 0;

    @Column(nullable = false)
    private Boolean verified = false;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime lastSentAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) {
            createdAt = now;
        }
        if (lastSentAt == null) {
            lastSentAt = now;
        }
        if (attempts == null) {
            attempts = 0;
        }
        if (verified == null) {
            verified = false;
        }
    }

    public OtpVerification() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }

    public String getOtpHash() {
        return otpHash;
    }

    public void setOtpHash(String otpHash) {
        this.otpHash = otpHash;
    }

    public String getChallengeId() {
        return challengeId;
    }

    public void setChallengeId(String challengeId) {
        this.challengeId = challengeId;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public Integer getAttempts() {
        return attempts != null ? attempts : 0;
    }

    public void setAttempts(Integer attempts) {
        this.attempts = attempts;
    }

    public Boolean getVerified() {
        return verified != null && verified;
    }

    public boolean isVerified() {
        return getVerified();
    }

    public void setVerified(Boolean verified) {
        this.verified = verified;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getLastSentAt() {
        return lastSentAt;
    }

    public void setLastSentAt(LocalDateTime lastSentAt) {
        this.lastSentAt = lastSentAt;
    }
}
