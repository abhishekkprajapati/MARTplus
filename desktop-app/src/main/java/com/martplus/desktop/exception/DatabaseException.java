package com.martplus.desktop.exception;

/**
 * Custom exception representing database errors, connection failures, or SQL transaction failures.
 */
public class DatabaseException extends ApplicationException {
    public DatabaseException(String message) {
        super("DATABASE_ERROR", message);
    }

    public DatabaseException(String message, Throwable cause) {
        super("DATABASE_ERROR", message, cause);
    }
}
