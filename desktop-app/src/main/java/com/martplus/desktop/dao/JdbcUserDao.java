package com.martplus.desktop.dao;

import com.martplus.desktop.exception.DatabaseException;
import com.martplus.desktop.model.User;

import java.sql.*;
import java.util.List;
import java.util.Optional;

/**
 * JDBC implementation of UserDao for PostgreSQL 'users' table.
 */
public class JdbcUserDao extends AbstractJdbcDao<User, Long> implements UserDao {

    @Override
    protected User mapResultSet(ResultSet rs) throws SQLException {
        Timestamp ts = rs.getTimestamp("created_at");
        return new User(
                rs.getLong("id"),
                rs.getString("first_name"),
                rs.getString("last_name"),
                rs.getString("email"),
                rs.getString("phone"),
                rs.getString("password"),
                rs.getString("role"),
                rs.getString("status"),
                rs.getBoolean("email_verified"),
                rs.getBoolean("phone_verified"),
                ts != null ? ts.toLocalDateTime() : null
        );
    }

    @Override
    public Optional<User> findById(Long id) {
        String sql = "SELECT * FROM users WHERE id = ?";
        return executeQueryForSingle(sql, id);
    }

    @Override
    public List<User> findAll() {
        String sql = "SELECT * FROM users ORDER BY id ASC";
        return executeQueryForList(sql);
    }

    @Override
    public Optional<User> findByEmailAndRole(String email, String role) {
        String sql = "SELECT * FROM users WHERE LOWER(email) = LOWER(?) AND UPPER(role) = UPPER(?)";
        return executeQueryForSingle(sql, email != null ? email.trim() : "", role != null ? role.trim() : "");
    }

    @Override
    public Optional<User> findByEmail(String email) {
        String sql = "SELECT * FROM users WHERE LOWER(email) = LOWER(?) LIMIT 1";
        return executeQueryForSingle(sql, email != null ? email.trim() : "");
    }

    @Override
    public List<User> findByRole(String role) {
        String sql = "SELECT * FROM users WHERE UPPER(role) = UPPER(?) ORDER BY id ASC";
        return executeQueryForList(sql, role != null ? role.trim() : "");
    }

    @Override
    public void updateStatus(Long userId, String status) {
        String sql = "UPDATE users SET status = ? WHERE id = ?";
        executeUpdate(sql, status, userId);
    }

    @Override
    public void updateRole(Long userId, String role) {
        String sql = "UPDATE users SET role = ? WHERE id = ?";
        executeUpdate(sql, role, userId);
    }

    @Override
    public User save(User user) {
        String sql = "INSERT INTO users (first_name, last_name, email, phone, password, role, status, email_verified, phone_verified, created_at) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?) RETURNING id";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            setParameters(stmt,
                    user.getFirstName(),
                    user.getLastName(),
                    user.getEmail().trim().toLowerCase(),
                    user.getPhone(),
                    user.getPassword(),
                    user.getRole(),
                    user.getStatus(),
                    user.isEmailVerified(),
                    user.isPhoneVerified(),
                    user.getCreatedAt() != null ? user.getCreatedAt() : java.time.LocalDateTime.now()
            );

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    user.setId(rs.getLong("id"));
                }
            }
            return user;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to insert user into PostgreSQL: " + e.getMessage(), e);
        }
    }

    @Override
    public void update(User user) {
        String sql = "UPDATE users SET first_name = ?, last_name = ?, phone = ?, role = ?, status = ? WHERE id = ?";
        executeUpdate(sql,
                user.getFirstName(),
                user.getLastName(),
                user.getPhone(),
                user.getRole(),
                user.getStatus(),
                user.getId()
        );
    }

    @Override
    public boolean deleteById(Long id) {
        String sql = "DELETE FROM users WHERE id = ?";
        return executeUpdate(sql, id) > 0;
    }
}
