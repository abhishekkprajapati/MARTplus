package com.martplus.desktop.exception;

/**
 * Custom exception representing authentication and authorization failures (invalid credentials, inactive user, role mismatch).
 */
public class AuthenticationException extends ApplicationException {
    public AuthenticationException(String message) {
        super("AUTH_ERROR", message);
    }

    public AuthenticationException(String message, Throwable cause) {
        super("AUTH_ERROR", message, cause);
    }
}
