package com.martplus.desktop.exception;

/**
 * Custom exception representing data validation failures (empty fields, invalid email format, negative values).
 */
public class ValidationException extends ApplicationException {
    public ValidationException(String message) {
        super("VALIDATION_ERROR", message);
    }

    public ValidationException(String message, Throwable cause) {
        super("VALIDATION_ERROR", message, cause);
    }
}
