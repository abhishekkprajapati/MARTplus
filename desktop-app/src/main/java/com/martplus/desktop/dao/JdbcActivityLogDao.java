package com.martplus.desktop.dao;

import com.martplus.desktop.exception.DatabaseException;
import com.martplus.desktop.model.ActivityLog;

import java.sql.*;
import java.util.List;
import java.util.Optional;

/**
 * JDBC implementation of ActivityLogDao for PostgreSQL 'activity_logs' table.
 */
public class JdbcActivityLogDao extends AbstractJdbcDao<ActivityLog, Long> implements ActivityLogDao {

    @Override
    protected ActivityLog mapResultSet(ResultSet rs) throws SQLException {
        Timestamp ts = rs.getTimestamp("created_at");
        ActivityLog log = new ActivityLog(
                rs.getLong("id"),
                rs.getObject("admin_id") != null ? rs.getLong("admin_id") : null,
                rs.getString("action"),
                rs.getString("description"),
                ts != null ? ts.toLocalDateTime() : null
        );

        try {
            String adminFirst = rs.getString("admin_first_name");
            String adminLast = rs.getString("admin_last_name");
            if (adminFirst != null || adminLast != null) {
                log.setAdminName(((adminFirst != null ? adminFirst : "") + " " + (adminLast != null ? adminLast : "")).trim());
            }
        } catch (SQLException ignored) {
        }

        return log;
    }

    private static final String SELECT_BASE =
            "SELECT a.*, u.first_name AS admin_first_name, u.last_name AS admin_last_name " +
            "FROM activity_logs a LEFT JOIN users u ON a.admin_id = u.id ";

    @Override
    public Optional<ActivityLog> findById(Long id) {
        String sql = SELECT_BASE + "WHERE a.id = ?";
        return executeQueryForSingle(sql, id);
    }

    @Override
    public List<ActivityLog> findAll() {
        String sql = SELECT_BASE + "ORDER BY a.created_at DESC, a.id DESC";
        return executeQueryForList(sql);
    }

    @Override
    public List<ActivityLog> findRecentLogs(int limit) {
        String sql = SELECT_BASE + "ORDER BY a.created_at DESC, a.id DESC LIMIT ?";
        return executeQueryForList(sql, limit > 0 ? limit : 50);
    }

    @Override
    public void logActivity(Long adminId, String action, String description) {
        String sql = "INSERT INTO activity_logs (admin_id, action, description, created_at) VALUES (?, ?, ?, ?)";
        executeUpdate(sql, adminId, action, description, java.time.LocalDateTime.now());
    }

    @Override
    public ActivityLog save(ActivityLog log) {
        String sql = "INSERT INTO activity_logs (admin_id, action, description, created_at) VALUES (?, ?, ?, ?) RETURNING id";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            setParameters(stmt,
                    log.getAdminId(),
                    log.getAction(),
                    log.getDescription(),
                    log.getCreatedAt() != null ? log.getCreatedAt() : java.time.LocalDateTime.now()
            );

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    log.setId(rs.getLong("id"));
                }
            }
            return log;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to save activity log: " + e.getMessage(), e);
        }
    }

    @Override
    public void update(ActivityLog entity) {
        // Activity logs are immutable audit records
    }

    @Override
    public boolean deleteById(Long id) {
        String sql = "DELETE FROM activity_logs WHERE id = ?";
        return executeUpdate(sql, id) > 0;
    }
}
