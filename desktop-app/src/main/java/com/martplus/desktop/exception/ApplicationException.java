package com.martplus.desktop.exception;

/**
 * Root custom runtime exception for the MARTplus desktop application.
 * Demonstrates hierarchical OOP Exception Handling.
 */
public class ApplicationException extends RuntimeException {
    private final String errorCode;

    public ApplicationException(String message) {
        super(message);
        this.errorCode = "APP_ERROR";
    }

    public ApplicationException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "APP_ERROR";
    }

    public ApplicationException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public ApplicationException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
