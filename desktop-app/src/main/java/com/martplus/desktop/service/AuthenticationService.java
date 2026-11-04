package com.martplus.desktop.service;

import com.martplus.desktop.config.AppConfig;
import com.martplus.desktop.dao.UserDao;
import com.martplus.desktop.exception.AuthenticationException;
import com.martplus.desktop.exception.ValidationException;
import com.martplus.desktop.model.User;
import com.martplus.desktop.util.PasswordUtil;
import com.martplus.desktop.util.ValidationUtil;

import java.util.Optional;

/**
 * Service handling user authentication and session management.
 * Verifies passwords against BCrypt hashes stored in PostgreSQL.
 */
public class AuthenticationService {

    private final UserDao userDao;

    public AuthenticationService(UserDao userDao) {
        this.userDao = userDao;
    }

    /**
     * Authenticates a user against PostgreSQL records using BCrypt hashing.
     *
     * @param email User email
     * @param rawPassword Plaintext password from UI
     * @param expectedRole Selected role (BUYER, SELLER, ADMIN)
     * @return Authenticated User model
     */
    public User login(String email, String rawPassword, String expectedRole) {
        ValidationUtil.validateEmail(email);
        ValidationUtil.requireNonEmpty(rawPassword, "Password");
        ValidationUtil.requireNonEmpty(expectedRole, "Account Type");

        String normalizedEmail = email.trim().toLowerCase();
        String normalizedRole = expectedRole.trim().toUpperCase();

        Optional<User> userOpt = userDao.findByEmailAndRole(normalizedEmail, normalizedRole);
        if (userOpt.isEmpty()) {
            throw new AuthenticationException("No account found with this email and role (" + normalizedRole + ").");
        }

        User user = userOpt.get();

        // Check password against BCrypt hash
        if (!PasswordUtil.checkPassword(rawPassword, user.getPassword())) {
            throw new AuthenticationException("Invalid email or password.");
        }

        // Mirror backend behavior: if PENDING_VERIFICATION, activate account upon valid login
        if ("PENDING_VERIFICATION".equalsIgnoreCase(user.getStatus())) {
            user.setStatus("ACTIVE");
            user.setEmailVerified(true);
            user.setPhoneVerified(true);
            userDao.updateStatus(user.getId(), "ACTIVE");
        }

        if (!user.isActive()) {
            throw new AuthenticationException("Your account is " + user.getStatus() + ". Please contact administrator.");
        }

        // Establish session in AppConfig
        AppConfig.setCurrentUser(user);
        return user;
    }

    public void logout() {
        AppConfig.clearSession();
    }
}
