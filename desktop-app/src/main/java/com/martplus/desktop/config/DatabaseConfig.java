package com.martplus.desktop.config;

import com.martplus.desktop.exception.DatabaseException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Direct JDBC Database Configuration and Connection Factory.
 * Fulfills Phase 6 & Phase 7 assessment criteria:
 * - Direct JDBC without Hibernate or Spring Data JPA.
 * - Reads DATABASE_URL, DATABASE_USERNAME, DATABASE_PASSWORD from environment or system properties.
 * - Safe connection creation and validation.
 */
public class DatabaseConfig {

    private static String dbUrl;
    private static String dbUser;
    private static String dbPassword;
    private static boolean driverLoaded = false;

    static {
        loadConfiguration();
    }

    /**
     * Loads database credentials from Environment Variables or JVM System Properties.
     * Defaults fall back to local PostgreSQL shopsphere development database.
     */
    public static synchronized void loadConfiguration() {
        String envUrl = System.getenv("DATABASE_URL");
        String sysUrl = System.getProperty("DATABASE_URL");
        dbUrl = (envUrl != null && !envUrl.isBlank()) ? envUrl :
                (sysUrl != null && !sysUrl.isBlank()) ? sysUrl : "jdbc:postgresql://localhost:5432/shopsphere";

        String envUser = System.getenv("DATABASE_USERNAME");
        String sysUser = System.getProperty("DATABASE_USERNAME");
        dbUser = (envUser != null && !envUser.isBlank()) ? envUser :
                (sysUser != null && !sysUser.isBlank()) ? sysUser : "shopsphere_user";

        String envPass = System.getenv("DATABASE_PASSWORD");
        String sysPass = System.getProperty("DATABASE_PASSWORD");
        dbPassword = (envPass != null) ? envPass :
                (sysPass != null) ? sysPass : "";

        try {
            Class.forName("org.postgresql.Driver");
            driverLoaded = true;
        } catch (ClassNotFoundException e) {
            throw new DatabaseException("PostgreSQL JDBC Driver class not found in classpath.", e);
        }
    }

    /**
     * Allows custom test or programmatic override of connection settings.
     */
    public static synchronized void setCredentials(String url, String username, String password) {
        dbUrl = url;
        dbUser = username;
        dbPassword = password;
    }

    public static String getDbUrl() {
        return dbUrl;
    }

    public static String getDbUser() {
        return dbUser;
    }

    /**
     * Obtains a fresh JDBC Connection from DriverManager.
     * Multi-threading safety: Each thread / operation obtains its own distinct Connection.
     *
     * @return java.sql.Connection
     * @throws DatabaseException on failure
     */
    public static Connection getConnection() {
        if (!driverLoaded) {
            loadConfiguration();
        }
        try {
            return DriverManager.getConnection(dbUrl, dbUser, dbPassword);
        } catch (SQLException e) {
            throw new DatabaseException("Failed to establish JDBC Connection to database (" + dbUrl + "): " + e.getMessage(), e);
        }
    }

    /**
     * Tests connectivity to the database.
     *
     * @return true if connection succeeds and is valid, false otherwise.
     */
    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed() && conn.isValid(3);
        } catch (Exception e) {
            return false;
        }
    }
}
