package com.ShopSphere.dto;

public class RegisterResponse {

    private Long userId;
    private String email;
    private String phone;
    private String role;
    private boolean emailVerified;
    private boolean phoneVerified;
    private boolean requiresVerification;
    private String message;

    public RegisterResponse() {
    }

    public RegisterResponse(Long userId, String email, String phone, String role, boolean emailVerified, boolean phoneVerified, boolean requiresVerification, String message) {
        this.userId = userId;
        this.email = email;
        this.phone = phone;
        this.role = role;
        this.emailVerified = emailVerified;
        this.phoneVerified = phoneVerified;
        this.requiresVerification = requiresVerification;
        this.message = message;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public boolean isEmailVerified() {
        return emailVerified;
    }

    public void setEmailVerified(boolean emailVerified) {
        this.emailVerified = emailVerified;
    }

    public boolean isPhoneVerified() {
        return phoneVerified;
    }

    public void setPhoneVerified(boolean phoneVerified) {
        this.phoneVerified = phoneVerified;
    }

    public boolean isRequiresVerification() {
        return requiresVerification;
    }

    public void setRequiresVerification(boolean requiresVerification) {
        this.requiresVerification = requiresVerification;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
