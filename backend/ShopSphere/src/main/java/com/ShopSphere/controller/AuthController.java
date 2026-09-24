package com.ShopSphere.controller;

import com.ShopSphere.dto.*;
import com.ShopSphere.entity.User;
import com.ShopSphere.service.AuthService;
import com.ShopSphere.service.OtpService;
import com.ShopSphere.service.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final AuthService authService;
    private final OtpService otpService;

    @Value("${otp.dev-mode:true}")
    private boolean devMode;

    public AuthController(
            UserService userService,
            AuthService authService,
            OtpService otpService
    ) {
        this.userService = userService;
        this.authService = authService;
        this.otpService = otpService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(
            @RequestBody RegisterRequest request
    ) {
        try {
            User user = new User();

            user.setFirstName(request.getFirstName());
            user.setLastName(request.getLastName());
            user.setEmail(request.getEmail());
            user.setPhone(request.getPhone());
            user.setPassword(request.getPassword());
            user.setRole(request.getRole());

            User savedUser = userService.registerUser(user);

            RegisterResponse response = new RegisterResponse(
                    savedUser.getId(),
                    savedUser.getEmail(),
                    savedUser.getPhone(),
                    savedUser.getRole(),
                    savedUser.getEmailVerified(),
                    savedUser.getPhoneVerified(),
                    false,
                    "Registration successful."
            );

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(response);

        } catch (RuntimeException e) {
            return ResponseEntity
                    .badRequest()
                    .body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(
            @RequestBody LoginRequest request
    ) {
        try {
            Object response = authService.login(request);
            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            String msg = e.getMessage();
            HttpStatus status = (msg != null && (msg.contains("verify") || msg.contains("active")))
                    ? HttpStatus.FORBIDDEN
                    : (msg != null && (msg.contains("not configured") || msg.contains("unavailable") || msg.contains("authentication failed") || msg.contains("configuration is invalid")))
                    ? HttpStatus.SERVICE_UNAVAILABLE
                    : HttpStatus.UNAUTHORIZED;

            return ResponseEntity
                    .status(status)
                    .body(Map.of("message", msg));
        }
    }

    @PostMapping("/verify-email")
    public ResponseEntity<?> verifyEmail(
            @RequestBody VerifyOtpRequest request
    ) {
        try {
            User user = userService.verifyEmail(request.getTarget(), request.getOtp());
            Map<String, Object> body = new HashMap<>();
            body.put("success", true);
            body.put("message", "Email verified successfully.");
            body.put("emailVerified", true);
            body.put("phoneVerified", user.getPhoneVerified());
            body.put("status", user.getStatus());
            return ResponseEntity.ok(body);
        } catch (RuntimeException e) {
            return ResponseEntity
                    .badRequest()
                    .body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/send-phone-otp")
    public ResponseEntity<?> sendPhoneOtp(
            @RequestBody Map<String, String> request
    ) {
        try {
            String rawPhone = request.get("phone");
            if (rawPhone == null || rawPhone.isBlank()) {
                rawPhone = request.get("target");
            }
            String normalizedPhone = userService.sendPhoneOtp(rawPhone);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "phone", normalizedPhone,
                    "message", "Verification code sent to your mobile number."
            ));
        } catch (RuntimeException e) {
            String msg = e.getMessage();
            HttpStatus status = (msg != null && msg.contains("wait"))
                    ? HttpStatus.TOO_MANY_REQUESTS
                    : (msg != null && msg.contains("already registered"))
                    ? HttpStatus.CONFLICT
                    : (msg != null && (msg.contains("not configured") || msg.contains("unavailable") || msg.contains("authentication failed") || msg.contains("configuration is invalid")))
                    ? HttpStatus.SERVICE_UNAVAILABLE
                    : HttpStatus.BAD_REQUEST;
            return ResponseEntity.status(status).body(Map.of("message", msg != null ? msg : "Unable to send verification code."));
        }
    }

    @PostMapping("/verify-phone")
    public ResponseEntity<?> verifyPhone(
            @RequestBody VerifyOtpRequest request
    ) {
        try {
            Map<String, Object> body = userService.verifyPhone(request.getTarget(), request.getOtp());
            return ResponseEntity.ok(body);
        } catch (RuntimeException e) {
            return ResponseEntity
                    .badRequest()
                    .body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/verify-login-otp")
    public ResponseEntity<?> verifyLoginOtp(
            @RequestBody VerifyLoginOtpRequest request
    ) {
        try {
            LoginResponse response = authService.verifyLoginOtp(request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/resend-email-otp")
    public ResponseEntity<?> resendEmailOtp(
            @RequestBody ResendOtpRequest request
    ) {
        try {
            userService.resendEmailOtp(request.getTarget());
            return ResponseEntity.ok(Map.of("success", true, "message", "Verification OTP sent to your email."));
        } catch (RuntimeException e) {
            return ResponseEntity
                    .badRequest()
                    .body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/resend-phone-otp")
    public ResponseEntity<?> resendPhoneOtp(
            @RequestBody ResendOtpRequest request
    ) {
        try {
            userService.resendPhoneOtp(request.getTarget());
            return ResponseEntity.ok(Map.of("success", true, "message", "Verification code sent to your mobile number."));
        } catch (RuntimeException e) {
            String msg = e.getMessage();
            HttpStatus status = (msg != null && msg.contains("wait"))
                    ? HttpStatus.TOO_MANY_REQUESTS
                    : (msg != null && (msg.contains("not configured") || msg.contains("unavailable") || msg.contains("authentication failed") || msg.contains("configuration is invalid")))
                    ? HttpStatus.SERVICE_UNAVAILABLE
                    : HttpStatus.BAD_REQUEST;
            return ResponseEntity
                    .status(status)
                    .body(Map.of("message", msg != null ? msg : "Unable to resend verification code."));
        }
    }

    @PostMapping("/resend-login-otp")
    public ResponseEntity<?> resendLoginOtp(
            @RequestBody ResendOtpRequest request
    ) {
        try {
            LoginChallengeResponse response = authService.resendLoginOtp(request.getChallengeId());
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            String msg = e.getMessage();
            HttpStatus status = (msg != null && msg.contains("wait"))
                    ? HttpStatus.TOO_MANY_REQUESTS
                    : (msg != null && (msg.contains("not configured") || msg.contains("unavailable") || msg.contains("authentication failed") || msg.contains("configuration is invalid")))
                    ? HttpStatus.SERVICE_UNAVAILABLE
                    : HttpStatus.BAD_REQUEST;
            return ResponseEntity
                    .status(status)
                    .body(Map.of("message", msg != null ? msg : "Unable to resend login code."));
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(
            @RequestBody ForgotPasswordRequest request
    ) {
        try {
            userService.forgotPassword(request.getEmail());
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "If an account exists for this email, a verification code has been sent."
            ));
        } catch (RuntimeException e) {
            String msg = e.getMessage();
            HttpStatus status = (msg != null && (msg.contains("wait") || msg.contains("too many") || msg.contains("Too many")))
                    ? HttpStatus.TOO_MANY_REQUESTS
                    : HttpStatus.BAD_REQUEST;
            return ResponseEntity.status(status).body(Map.of("message", msg != null ? msg : "Unable to process request"));
        }
    }

    @PostMapping("/verify-password-reset")
    public ResponseEntity<?> verifyPasswordReset(
            @RequestBody VerifyPasswordResetRequest request
    ) {
        try {
            Map<String, Object> body = userService.verifyPasswordReset(request.getEmail(), request.getOtp());
            return ResponseEntity.ok(body);
        } catch (RuntimeException e) {
            return ResponseEntity
                    .badRequest()
                    .body(Map.of("message", e.getMessage() != null ? e.getMessage() : "Verification failed"));
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(
            @RequestBody ResetPasswordRequest request
    ) {
        try {
            userService.resetPassword(
                    request.getEmail(),
                    request.getResetToken(),
                    request.getNewPassword(),
                    request.getConfirmPassword()
            );
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Password reset successfully. You can now sign in."
            ));
        } catch (RuntimeException e) {
            return ResponseEntity
                    .badRequest()
                    .body(Map.of("message", e.getMessage() != null ? e.getMessage() : "Password reset failed"));
        }
    }

    @PostMapping("/resend-password-reset")
    public ResponseEntity<?> resendPasswordReset(
            @RequestBody ForgotPasswordRequest request
    ) {
        try {
            userService.resendPasswordReset(request.getEmail());
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "If an account exists for this email, a verification code has been sent."
            ));
        } catch (RuntimeException e) {
            String msg = e.getMessage();
            HttpStatus status = (msg != null && (msg.contains("wait") || msg.contains("too many") || msg.contains("Too many")))
                    ? HttpStatus.TOO_MANY_REQUESTS
                    : HttpStatus.BAD_REQUEST;
            return ResponseEntity.status(status).body(Map.of("message", msg != null ? msg : "Unable to process request"));
        }
    }

    /**
     * Development-only endpoint to inspect latest generated OTP for integration testing.
     * Returns 404 in production when devMode=false.
     */
    @GetMapping("/dev/latest-otp")
    public ResponseEntity<?> getDevLatestOtp(
            @RequestParam(required = false) String key,
            @RequestParam(required = false) String target,
            @RequestParam(required = false) String purpose) {
        if (!devMode) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        String lookupKey = key;
        if (lookupKey == null || lookupKey.isBlank()) {
            if (target != null && purpose != null) {
                lookupKey = (target.trim() + ":" + purpose.trim()).toLowerCase();
            } else if (target != null) {
                lookupKey = target.trim().toLowerCase();
            }
        }
        if (lookupKey == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Target or key parameter required"));
        }
        String otp = otpService.getDevOtp(lookupKey);
        if (otp == null && target != null) {
            otp = otpService.getDevOtp(target.trim().toLowerCase());
        }
        if (otp == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "No dev OTP found for key: " + lookupKey));
        }
        return ResponseEntity.ok(Map.of("key", lookupKey, "otp", otp));
    }
}
