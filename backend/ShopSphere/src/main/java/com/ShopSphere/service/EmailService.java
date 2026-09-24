package com.ShopSphere.service;

public interface EmailService {

    void sendEmailVerificationOtp(String toEmail, String firstName, String otp);

    void sendPasswordResetOtp(String toEmail, String firstName, String otp);

    void sendGeneralEmail(String toEmail, String subject, String htmlContent);
}
