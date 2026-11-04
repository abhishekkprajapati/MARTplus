package com.martplus.desktop.util;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Utility class for BCrypt password hashing and verification.
 * Fully compatible with Spring Security's BCryptPasswordEncoder ($2a$ and $2b$ salts).
 */
public final class PasswordUtil {

    private PasswordUtil() {
        // Prevent instantiation of utility class
    }

    /**
     * Hashes a raw plaintext password using BCrypt with a cost factor of 10.
     *
     * @param plainPassword Raw user password.
     * @return Formatted BCrypt hash string.
     */
    public static String hashPassword(String plainPassword) {
        if (plainPassword == null || plainPassword.isEmpty()) {
            throw new IllegalArgumentException("Password cannot be empty for hashing");
        }
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt(10));
    }

    /**
     * Verifies a raw plaintext password against a stored BCrypt hash.
     * Supports both $2a$ (jBCrypt standard) and $2b$ (Spring Security / OpenBSD) prefixes.
     *
     * @param plainPassword Raw user password entered during login.
     * @param hashedPassword Stored BCrypt hash from PostgreSQL users table.
     * @return True if credentials match, false otherwise.
     */
    public static boolean checkPassword(String plainPassword, String hashedPassword) {
        if (plainPassword == null || hashedPassword == null || hashedPassword.isEmpty()) {
            return false;
        }

        try {
            // Spring Security 6 generates "$2b$" prefixes. jBCrypt supports "$2a$" prefixes.
            // Converting "$2b$" to "$2a$" preserves the 100% identical salt and hash calculation.
            String normalizedHash = hashedPassword;
            if (normalizedHash.startsWith("$2b$")) {
                normalizedHash = "$2a$" + normalizedHash.substring(4);
            } else if (normalizedHash.startsWith("$2y$")) {
                normalizedHash = "$2a$" + normalizedHash.substring(4);
            }

            return BCrypt.checkpw(plainPassword, normalizedHash);
        } catch (Exception e) {
            return false;
        }
    }
}
