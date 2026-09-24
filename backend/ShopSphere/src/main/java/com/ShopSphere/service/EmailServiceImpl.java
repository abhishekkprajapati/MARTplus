package com.ShopSphere.service;

import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailServiceImpl implements EmailService {

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Value("${spring.mail.username:noreply@shopsphere.com}")
    private String fromEmail;

    @Value("${email.dev-mode:true}")
    private boolean devMode;

    @Override
    public void sendEmailVerificationOtp(String toEmail, String firstName, String otp) {
        String subject = "Verify your ShopSphere Account";
        String greetingName = (firstName != null && !firstName.isBlank()) ? firstName : "User";

        String htmlContent = "<!DOCTYPE html><html><body style='font-family: Arial, sans-serif; background-color: #f4f6f9; margin: 0; padding: 20px;'>"
                + "<div style='max-width: 520px; margin: 0 auto; background: #ffffff; border-radius: 12px; padding: 32px; box-shadow: 0 4px 12px rgba(0,0,0,0.08);'>"
                + "<h2 style='color: #4f46e5; margin-top: 0;'>ShopSphere Verification</h2>"
                + "<p>Hello <strong>" + greetingName + "</strong>,</p>"
                + "<p>Thank you for registering on ShopSphere. To complete your account verification, please enter the following one-time password (OTP):</p>"
                + "<div style='text-align: center; margin: 24px 0;'>"
                + "<span style='display: inline-block; font-size: 32px; font-weight: 800; letter-spacing: 6px; color: #111827; background: #f1f5f9; padding: 12px 28px; border-radius: 8px; border: 1px solid #cbd5e1;'>"
                + otp + "</span></div>"
                + "<p style='color: #64748b; font-size: 14px;'>This OTP is valid for <strong>10 minutes</strong>. Never share this code with anyone.</p>"
                + "<hr style='border: none; border-top: 1px solid #e2e8f0; margin: 24px 0;' />"
                + "<p style='color: #94a3b8; font-size: 12px; margin-bottom: 0;'>If you did not request this verification, please disregard this message.</p>"
                + "</div></body></html>";

        sendGeneralEmail(toEmail, subject, htmlContent);
    }

    @Override
    public void sendPasswordResetOtp(String toEmail, String firstName, String otp) {
        String subject = "ShopSphere Password Reset";
        String greetingName = (firstName != null && !firstName.isBlank()) ? firstName : "User";

        String htmlContent = "<!DOCTYPE html><html><body style='font-family: Arial, sans-serif; background-color: #f4f6f9; margin: 0; padding: 20px;'>"
                + "<div style='max-width: 520px; margin: 0 auto; background: #ffffff; border-radius: 12px; padding: 32px; box-shadow: 0 4px 12px rgba(0,0,0,0.08);'>"
                + "<h2 style='color: #4f46e5; margin-top: 0;'>ShopSphere Password Reset</h2>"
                + "<p>Hello <strong>" + greetingName + "</strong>,</p>"
                + "<p>We received a request to reset your ShopSphere password. Your verification code is:</p>"
                + "<div style='text-align: center; margin: 24px 0;'>"
                + "<span style='display: inline-block; font-size: 32px; font-weight: 800; letter-spacing: 6px; color: #111827; background: #f1f5f9; padding: 12px 28px; border-radius: 8px; border: 1px solid #cbd5e1;'>"
                + otp + "</span></div>"
                + "<p style='color: #64748b; font-size: 14px;'>This code expires in <strong>10 minutes</strong>. Never share this code with anyone.</p>"
                + "<hr style='border: none; border-top: 1px solid #e2e8f0; margin: 24px 0;' />"
                + "<p style='color: #94a3b8; font-size: 12px; margin-bottom: 0;'>If you did not request this password reset, please ignore this email.</p>"
                + "<p style='color: #64748b; font-size: 13px; margin-top: 12px;'>Regards,<br><strong>ShopSphere Team</strong></p>"
                + "</div></body></html>";

        sendGeneralEmail(toEmail, subject, htmlContent);
    }

    @Override
    public void sendGeneralEmail(String toEmail, String subject, String htmlContent) {
        if (devMode || mailSender == null) {
            System.out.println("[EMAIL SERVICE (DEV MODE)] Dispatching to: " + toEmail + " | Subject: " + subject);
            return;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            mailSender.send(message);
        } catch (Exception e) {
            System.err.println("Warning: Real email dispatch failed: " + e.getMessage());
            if (!devMode) {
                throw new RuntimeException("Failed to send verification email. Please try again later.");
            }
        }
    }
}
