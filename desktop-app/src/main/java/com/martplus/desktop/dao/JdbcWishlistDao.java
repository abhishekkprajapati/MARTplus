package com.martplus.desktop.dao;

import com.martplus.desktop.exception.DatabaseException;
import com.martplus.desktop.model.Product;
import com.martplus.desktop.model.WishlistItem;

import java.sql.*;
import java.util.List;
import java.util.Optional;

/**
 * JDBC implementation of WishlistDao for PostgreSQL 'wishlist_items' table.
 */
public class JdbcWishlistDao extends AbstractJdbcDao<WishlistItem, Long> implements WishlistDao {

    @Override
    protected WishlistItem mapResultSet(ResultSet rs) throws SQLException {
        Timestamp createdAt = rs.getTimestamp("created_at");
        WishlistItem item = new WishlistItem(
                rs.getLong("id"),
                rs.getLong("buyer_id"),
                rs.getLong("product_id"),
                createdAt != null ? createdAt.toLocalDateTime() : null
        );

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
        }

        return item;
    }

    private static final String SELECT_JOINED =
            "SELECT w.*, p.id AS prod_id, p.name AS prod_name, p.price AS prod_price, " +
            "p.stock AS prod_stock, p.category AS prod_category, p.status AS prod_status, p.image_url AS prod_image_url " +
            "FROM wishlist_items w JOIN products p ON w.product_id = p.id ";

    @Override
    public Optional<WishlistItem> findById(Long id) {
        String sql = SELECT_JOINED + "WHERE w.id = ?";
        return executeQueryForSingle(sql, id);
    }

    @Override
    public List<WishlistItem> findAll() {
        String sql = SELECT_JOINED + "ORDER BY w.id DESC";
        return executeQueryForList(sql);
    }

    @Override
    public List<WishlistItem> findByBuyerId(Long buyerId) {
        String sql = SELECT_JOINED + "WHERE w.buyer_id = ? ORDER BY w.created_at DESC";
        return executeQueryForList(sql, buyerId);
    }

    @Override
    public boolean existsByBuyerIdAndProductId(Long buyerId, Long productId) {
        String sql = "SELECT 1 FROM wishlist_items WHERE buyer_id = ? AND product_id = ? LIMIT 1";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, buyerId);
            stmt.setLong(2, productId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to check wishlist existence: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteByBuyerIdAndProductId(Long buyerId, Long productId) {
        String sql = "DELETE FROM wishlist_items WHERE buyer_id = ? AND product_id = ?";
        executeUpdate(sql, buyerId, productId);
    }

    @Override
    public WishlistItem save(WishlistItem item) {
        String sql = "INSERT INTO wishlist_items (buyer_id, product_id, created_at) VALUES (?, ?, ?) " +
                     "ON CONFLICT (buyer_id, product_id) DO NOTHING RETURNING id";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            setParameters(stmt,
                    item.getBuyerId(),
                    item.getProductId(),
                    item.getCreatedAt() != null ? item.getCreatedAt() : java.time.LocalDateTime.now()
            );

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    item.setId(rs.getLong("id"));
                }
            }
            return item;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to save wishlist item: " + e.getMessage(), e);
        }
    }

    @Override
    public void update(WishlistItem entity) {
        // Wishlist items have fixed product & buyer references; no update needed
    }

    @Override
    public boolean deleteById(Long id) {
        String sql = "DELETE FROM wishlist_items WHERE id = ?";
        return executeUpdate(sql, id) > 0;
    }
}
