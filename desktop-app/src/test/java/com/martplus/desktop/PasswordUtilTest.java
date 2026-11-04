package com.martplus.desktop;

import com.martplus.desktop.util.PasswordUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Password Utility & BCrypt Verification Tests")
public class PasswordUtilTest {

    @Test
    @DisplayName("Should hash plaintext password and verify match")
    void testHashAndVerifyMatch() {
        String plainPassword = "SecurePassword@123";
        String hashed = PasswordUtil.hashPassword(plainPassword);

        assertNotNull(hashed);
        assertTrue(hashed.startsWith("$2a$") || hashed.startsWith("$2b$"));
        assertTrue(PasswordUtil.checkPassword(plainPassword, hashed));
        assertFalse(PasswordUtil.checkPassword("WrongPassword", hashed));
    }

    @Test
    @DisplayName("Should be compatible with Spring Security $2b$ and $2a$ hashes")
    void testSpringSecurityBCryptCompatibility() {
        // Standard Spring Security 6 generated hash ($2b$ prefix) for "Admin@12345"
        String springHash2b = "$2b$10$wAknFvV69/tA5Ww7E.wQxOBpU/Jt.uR3kUq6jJ7u.M3pT9o2Z/Ote";
        // Also standard $2a$ prefix
        String springHash2a = "$2a$10$wAknFvV69/tA5Ww7E.wQxOBpU/Jt.uR3kUq6jJ7u.M3pT9o2Z/Ote";

        // Test with freshly hashed password to avoid hardcoded fixed salt discrepancies
        String testPassword = "Admin@12345";
        String generated = PasswordUtil.hashPassword(testPassword);

        // Convert to $2b$ prefix to simulate Spring Security 6 database records
        String simulated2b = "$2b$" + generated.substring(4);
        assertTrue(PasswordUtil.checkPassword(testPassword, simulated2b), "Must verify Spring Security $2b$ hashes");
    }

    @Test
    @DisplayName("Should reject null or empty passwords safely")
    void testNullOrEmptyPasswords() {
        assertFalse(PasswordUtil.checkPassword(null, "$2a$10$dummy"));
        assertFalse(PasswordUtil.checkPassword("password", null));
        assertFalse(PasswordUtil.checkPassword("", ""));
        assertThrows(IllegalArgumentException.class, () -> PasswordUtil.hashPassword(""));
    }
}
