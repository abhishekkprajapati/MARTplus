package com.martplus.desktop;

import com.martplus.desktop.exception.ValidationException;
import com.martplus.desktop.util.ValidationUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Validation Utility Tests")
public class ValidationUtilTest {

    @Test
    @DisplayName("Should accept valid emails and reject invalid emails")
    void testEmailValidation() {
        assertDoesNotThrow(() -> ValidationUtil.validateEmail("buyer@example.com"));
        assertDoesNotThrow(() -> ValidationUtil.validateEmail("john.doe+test@domain.co.in"));

        assertThrows(ValidationException.class, () -> ValidationUtil.validateEmail(""));
        assertThrows(ValidationException.class, () -> ValidationUtil.validateEmail("invalid-email"));
        assertThrows(ValidationException.class, () -> ValidationUtil.validateEmail("missing@domain"));
    }

    @Test
    @DisplayName("Should validate 10-digit phone numbers")
    void testPhoneValidation() {
        assertDoesNotThrow(() -> ValidationUtil.validatePhone("9876543210"));
        assertDoesNotThrow(() -> ValidationUtil.validatePhone(null)); // Optional
        assertDoesNotThrow(() -> ValidationUtil.validatePhone(""));   // Optional empty

        assertThrows(ValidationException.class, () -> ValidationUtil.validatePhone("12345"));
        assertThrows(ValidationException.class, () -> ValidationUtil.validatePhone("phone12345"));
        assertThrows(ValidationException.class, () -> ValidationUtil.validatePhone("987654321000"));
    }

    @Test
    @DisplayName("Should enforce positive prices and non-negative stock")
    void testPriceAndStockValidation() {
        assertDoesNotThrow(() -> ValidationUtil.validatePositivePrice(BigDecimal.valueOf(199.99), "Price"));
        assertThrows(ValidationException.class, () -> ValidationUtil.validatePositivePrice(BigDecimal.ZERO, "Price"));
        assertThrows(ValidationException.class, () -> ValidationUtil.validatePositivePrice(BigDecimal.valueOf(-10), "Price"));

        assertDoesNotThrow(() -> ValidationUtil.validateNonNegativeStock(0, "Stock"));
        assertDoesNotThrow(() -> ValidationUtil.validateNonNegativeStock(50, "Stock"));
        assertThrows(ValidationException.class, () -> ValidationUtil.validateNonNegativeStock(-1, "Stock"));
    }
}
