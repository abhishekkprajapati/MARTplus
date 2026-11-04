package com.martplus.desktop.dao;

import com.martplus.desktop.config.DatabaseConfig;
import com.martplus.desktop.exception.DatabaseException;
import com.martplus.desktop.model.Product;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * JDBC implementation of InventoryDao for stock operations on PostgreSQL 'products'.
 */
public class JdbcInventoryDao implements InventoryDao {

    protected Connection getConnection() throws SQLException {
        return DatabaseConfig.getConnection();
    }

    @Override
    public int getStock(Long productId) {
        String sql = "SELECT stock FROM products WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, productId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to read product stock: " + e.getMessage(), e);
        }
        return 0;
    }

    @Override
    public void updateStock(Long productId, int newStock) {
        String sql = "UPDATE products SET stock = ? WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, Math.max(0, newStock));
            stmt.setLong(2, productId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Failed to update inventory stock: " + e.getMessage(), e);
        }
    }

    @Override
    public void deductStock(Connection conn, Long productId, int quantity) {
        // Enforce non-negative stock constraint at the SQL level
        String sql = "UPDATE products SET stock = stock - ? WHERE id = ? AND stock >= ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, quantity);
            stmt.setLong(2, productId);
            stmt.setInt(3, quantity);
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new DatabaseException("Insufficient stock to deduct " + quantity + " units for product ID " + productId);
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to deduct product stock in transaction: " + e.getMessage(), e);
        }
    }

    @Override
    public void addStock(Long productId, int quantity) {
        String sql = "UPDATE products SET stock = stock + ? WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, Math.max(0, quantity));
            stmt.setLong(2, productId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Failed to add inventory stock: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Product> findLowStockProducts(Long sellerId, int threshold) {
        StringBuilder sql = new StringBuilder("SELECT * FROM products WHERE stock <= ? AND UPPER(status) = 'ACTIVE' ");
        if (sellerId != null) {
            sql.append("AND seller_id = ? ");
        }
        sql.append("ORDER BY stock ASC");

        List<Product> list = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            stmt.setInt(1, threshold);
            if (sellerId != null) {
                stmt.setLong(2, sellerId);
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Timestamp ts = rs.getTimestamp("created_at");
                    list.add(new Product(
                            rs.getLong("id"),
                            rs.getString("name"),
                            rs.getString("description"),
                            rs.getBigDecimal("price"),
                            rs.getInt("stock"),
                            rs.getString("category"),
                            rs.getString("image_url"),
                            rs.getString("status"),
                            rs.getObject("seller_id") != null ? rs.getLong("seller_id") : null,
                            ts != null ? ts.toLocalDateTime() : null
                    ));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to query low stock products: " + e.getMessage(), e);
        }
        return list;
    }
}
