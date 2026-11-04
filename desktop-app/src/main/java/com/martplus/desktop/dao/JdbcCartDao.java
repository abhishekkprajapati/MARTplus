package com.martplus.desktop.dao;

import com.martplus.desktop.exception.DatabaseException;
import com.martplus.desktop.model.CartItem;
import com.martplus.desktop.model.Product;

import java.sql.*;
import java.util.List;
import java.util.Optional;

/**
 * JDBC implementation of CartDao for PostgreSQL 'cart_items' table.
 */
public class JdbcCartDao extends AbstractJdbcDao<CartItem, Long> implements CartDao {

    @Override
    protected CartItem mapResultSet(ResultSet rs) throws SQLException {
        Timestamp createdAt = rs.getTimestamp("created_at");
        Timestamp updatedAt = rs.getTimestamp("updated_at");

        CartItem item = new CartItem(
                rs.getLong("id"),
                rs.getLong("buyer_id"),
                rs.getLong("product_id"),
                rs.getInt("quantity"),
                createdAt != null ? createdAt.toLocalDateTime() : null,
                updatedAt != null ? updatedAt.toLocalDateTime() : null
        );

        // Map joined product fields if present
        try {
            Long prodId = rs.getObject("prod_id") != null ? rs.getLong("prod_id") : rs.getLong("product_id");
            String prodName = rs.getString("prod_name");
            if (prodName != null) {
                Product p = new Product();
                p.setId(prodId);
                p.setName(prodName);
                p.setPrice(rs.getBigDecimal("prod_price"));
                p.setStock(rs.getInt("prod_stock"));
                p.setCategory(rs.getString("prod_category"));
                p.setStatus(rs.getString("prod_status"));
                p.setImageUrl(rs.getString("prod_image_url"));
                item.setProduct(p);
            }
        } catch (SQLException ignored) {
            // Columns not in query
        }

        return item;
    }

    private static final String SELECT_JOINED =
            "SELECT c.*, p.id AS prod_id, p.name AS prod_name, p.price AS prod_price, " +
            "p.stock AS prod_stock, p.category AS prod_category, p.status AS prod_status, p.image_url AS prod_image_url " +
            "FROM cart_items c JOIN products p ON c.product_id = p.id ";

    @Override
    public Optional<CartItem> findById(Long id) {
        String sql = SELECT_JOINED + "WHERE c.id = ?";
        return executeQueryForSingle(sql, id);
    }

    @Override
    public List<CartItem> findAll() {
        String sql = SELECT_JOINED + "ORDER BY c.id DESC";
        return executeQueryForList(sql);
    }

    @Override
    public List<CartItem> findByBuyerId(Long buyerId) {
        String sql = SELECT_JOINED + "WHERE c.buyer_id = ? ORDER BY c.created_at DESC";
        return executeQueryForList(sql, buyerId);
    }

    @Override
    public Optional<CartItem> findByBuyerIdAndProductId(Long buyerId, Long productId) {
        String sql = SELECT_JOINED + "WHERE c.buyer_id = ? AND c.product_id = ?";
        return executeQueryForSingle(sql, buyerId, productId);
    }

    @Override
    public void updateQuantity(Long cartItemId, int quantity) {
        String sql = "UPDATE cart_items SET quantity = ?, updated_at = ? WHERE id = ?";
        executeUpdate(sql, Math.max(1, quantity), java.time.LocalDateTime.now(), cartItemId);
    }

    @Override
    public void deleteByBuyerId(Long buyerId) {
        String sql = "DELETE FROM cart_items WHERE buyer_id = ?";
        executeUpdate(sql, buyerId);
    }

    @Override
    public void deleteByBuyerId(Connection conn, Long buyerId) {
        String sql = "DELETE FROM cart_items WHERE buyer_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, buyerId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Failed to clear cart in transaction: " + e.getMessage(), e);
        }
    }

    @Override
    public int countByBuyerId(Long buyerId) {
        String sql = "SELECT COALESCE(SUM(quantity), 0) FROM cart_items WHERE buyer_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, buyerId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to count cart items: " + e.getMessage(), e);
        }
        return 0;
    }

    @Override
    public CartItem save(CartItem item) {
        String sql = "INSERT INTO cart_items (buyer_id, product_id, quantity, created_at, updated_at) " +
                     "VALUES (?, ?, ?, ?, ?) " +
                     "ON CONFLICT (buyer_id, product_id) DO UPDATE SET quantity = cart_items.quantity + EXCLUDED.quantity, updated_at = EXCLUDED.updated_at " +
                     "RETURNING id";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            java.time.LocalDateTime now = java.time.LocalDateTime.now();
            setParameters(stmt,
                    item.getBuyerId(),
                    item.getProductId(),
                    item.getQuantity(),
                    item.getCreatedAt() != null ? item.getCreatedAt() : now,
                    now
            );

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    item.setId(rs.getLong("id"));
                }
            }
            return item;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to save cart item: " + e.getMessage(), e);
        }
    }

    @Override
    public void update(CartItem item) {
        updateQuantity(item.getId(), item.getQuantity());
    }

    @Override
    public boolean deleteById(Long id) {
        String sql = "DELETE FROM cart_items WHERE id = ?";
        return executeUpdate(sql, id) > 0;
    }
}
