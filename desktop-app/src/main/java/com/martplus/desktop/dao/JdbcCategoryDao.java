package com.martplus.desktop.dao;

import com.martplus.desktop.exception.DatabaseException;
import com.martplus.desktop.model.Category;

import java.sql.*;
import java.util.List;
import java.util.Optional;

/**
 * JDBC implementation of CategoryDao for PostgreSQL 'categories' table.
 */
public class JdbcCategoryDao extends AbstractJdbcDao<Category, Long> implements CategoryDao {

    @Override
    protected Category mapResultSet(ResultSet rs) throws SQLException {
        Timestamp ts = rs.getTimestamp("created_at");
        return new Category(
                rs.getLong("id"),
                rs.getString("name"),
                rs.getString("description"),
                rs.getString("status"),
                ts != null ? ts.toLocalDateTime() : null
        );
    }

    @Override
    public Optional<Category> findById(Long id) {
        String sql = "SELECT * FROM categories WHERE id = ?";
        return executeQueryForSingle(sql, id);
    }

    @Override
    public List<Category> findAll() {
        String sql = "SELECT * FROM categories ORDER BY name ASC";
        return executeQueryForList(sql);
    }

    @Override
    public Optional<Category> findByName(String name) {
        String sql = "SELECT * FROM categories WHERE LOWER(name) = LOWER(?)";
        return executeQueryForSingle(sql, name != null ? name.trim() : "");
    }

    @Override
    public List<Category> findActiveCategories() {
        String sql = "SELECT * FROM categories WHERE UPPER(status) = 'ACTIVE' ORDER BY name ASC";
        return executeQueryForList(sql);
    }

    @Override
    public Category save(Category category) {
        String sql = "INSERT INTO categories (name, description, status, created_at) VALUES (?, ?, ?, ?) RETURNING id";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            setParameters(stmt,
                    category.getName(),
                    category.getDescription(),
                    category.getStatus() != null ? category.getStatus() : "ACTIVE",
                    category.getCreatedAt() != null ? category.getCreatedAt() : java.time.LocalDateTime.now()
            );

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    category.setId(rs.getLong("id"));
                }
            }
            return category;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to insert category: " + e.getMessage(), e);
        }
    }

    @Override
    public void update(Category category) {
        String sql = "UPDATE categories SET name = ?, description = ?, status = ? WHERE id = ?";
        executeUpdate(sql, category.getName(), category.getDescription(), category.getStatus(), category.getId());
    }

    @Override
    public boolean deleteById(Long id) {
        String sql = "DELETE FROM categories WHERE id = ?";
        return executeUpdate(sql, id) > 0;
    }
}
