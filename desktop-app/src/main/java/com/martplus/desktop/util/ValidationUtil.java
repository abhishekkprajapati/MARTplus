package com.martplus.desktop.util;

import com.martplus.desktop.exception.ValidationException;

import java.math.BigDecimal;
import java.util.regex.Pattern;

/**
 * Utility class providing validation methods for inputs across UI and Services.
 */
public final class ValidationUtil {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$");

    private static final Pattern PHONE_PATTERN =
            Pattern.compile("^[0-9]{10}$");

    private ValidationUtil() {
    }

    public static void requireNonEmpty(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new ValidationException(fieldName + " is required and cannot be empty.");
        }
    }

    public static void validateEmail(String email) {
        requireNonEmpty(email, "Email address");
        if (!EMAIL_PATTERN.matcher(email.trim()).matches()) {
            throw new ValidationException("Invalid email address format.");
        }
    }

    public static void validatePhone(String phone) {
        if (phone != null && !phone.trim().isEmpty()) {
            if (!PHONE_PATTERN.matcher(phone.trim()).matches()) {
                throw new ValidationException("Phone number must be exactly 10 digits.");
            }
        }
    }

    public static void validatePositivePrice(BigDecimal price, String fieldName) {
        if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException(fieldName + " must be greater than zero.");
        }
    }

    public static void validateNonNegativeStock(Integer stock, String fieldName) {
        if (stock == null || stock < 0) {
            throw new ValidationException(fieldName + " cannot be negative.");
        }
    }

    public static void validatePositiveQuantity(Integer quantity, String fieldName) {
        if (quantity == null || quantity <= 0) {
            throw new ValidationException(fieldName + " must be at least 1.");
        }
    }
}
