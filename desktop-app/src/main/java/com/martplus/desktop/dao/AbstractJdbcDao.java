package com.martplus.desktop.dao;

import com.martplus.desktop.config.DatabaseConfig;
import com.martplus.desktop.exception.DatabaseException;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Abstract Generic JDBC DAO base class.
 * Demonstrates:
 * 1. Java Generics: Type parameters <T, ID>.
 * 2. JDBC try-with-resources for leak-free Statement and ResultSet closing.
 * 3. Parameterized PreparedStatements preventing SQL Injection.
 * 4. Mapping SQLExceptions to Application DatabaseException.
 */
public abstract class AbstractJdbcDao<T, ID> implements GenericDao<T, ID> {

    /**
     * Provides a database connection.
     * Overridable for unit testing or transaction context propagation.
     */
    protected Connection getConnection() throws SQLException {
        return DatabaseConfig.getConnection();
    }

    /**
     * Abstract template method: concrete DAOs map a ResultSet row into an entity model.
     */
    protected abstract T mapResultSet(ResultSet rs) throws SQLException;

    /**
     * Reusable generic query method that returns a List of mapped entities.
     */
    protected List<T> executeQueryForList(String sql, Object... params) {
        List<T> results = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            setParameters(stmt, params);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    results.add(mapResultSet(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to execute JDBC query: " + sql, e);
        }
        return results;
    }

    /**
     * Reusable generic query method that returns a single optional entity.
     */
    protected Optional<T> executeQueryForSingle(String sql, Object... params) {
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            setParameters(stmt, params);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSet(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to query single record: " + sql, e);
        }
        return Optional.empty();
    }

    /**
     * Reusable DML execution method for INSERT, UPDATE, DELETE.
     */
    protected int executeUpdate(String sql, Object... params) {
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            setParameters(stmt, params);
            return stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Failed to execute JDBC update: " + sql, e);
        }
    }

    /**
     * Safely binds parameters to PreparedStatement to prevent SQL injection.
     */
    protected void setParameters(PreparedStatement stmt, Object... params) throws SQLException {
        if (params != null) {
            for (int i = 0; i < params.length; i++) {
                Object param = params[i];
                if (param instanceof java.time.LocalDateTime ldt) {
                    stmt.setTimestamp(i + 1, Timestamp.valueOf(ldt));
                } else {
                    stmt.setObject(i + 1, param);
                }
            }
        }
    }
}
