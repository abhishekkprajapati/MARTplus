package com.martplus.desktop.dao;

import com.martplus.desktop.exception.DatabaseException;
import com.martplus.desktop.model.Product;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * JDBC implementation of ProductDao for PostgreSQL 'products' table.
 */
public class JdbcProductDao extends AbstractJdbcDao<Product, Long> implements ProductDao {

    @Override
    protected Product mapResultSet(ResultSet rs) throws SQLException {
        Timestamp ts = rs.getTimestamp("created_at");
        Product p = new Product(
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
        );

        // Map seller name if joined
        try {
            String sellerFirst = rs.getString("seller_first_name");
            String sellerLast = rs.getString("seller_last_name");
            if (sellerFirst != null || sellerLast != null) {
                p.setSellerName(((sellerFirst != null ? sellerFirst : "") + " " + (sellerLast != null ? sellerLast : "")).trim());
            }
        } catch (SQLException ignored) {
            // Column may not be in result set if not joined
        }

        return p;
    }

    private static final String SELECT_BASE =
            "SELECT p.*, u.first_name AS seller_first_name, u.last_name AS seller_last_name " +
            "FROM products p LEFT JOIN users u ON p.seller_id = u.id ";

    @Override
    public Optional<Product> findById(Long id) {
        String sql = SELECT_BASE + "WHERE p.id = ?";
        return executeQueryForSingle(sql, id);
    }

    @Override
    public List<Product> findAll() {
        String sql = SELECT_BASE + "ORDER BY p.id DESC";
        return executeQueryForList(sql);
    }

    @Override
    public List<Product> findBySellerId(Long sellerId) {
        String sql = SELECT_BASE + "WHERE p.seller_id = ? ORDER BY p.id DESC";
        return executeQueryForList(sql, sellerId);
    }

    @Override
    public List<Product> findAvailableProducts() {
        String sql = SELECT_BASE + "WHERE UPPER(p.status) = 'ACTIVE' AND p.stock > 0 ORDER BY p.id DESC";
        return executeQueryForList(sql);
    }

    @Override
    public List<Product> searchByNameAndCategory(String keyword, String category) {
        StringBuilder sql = new StringBuilder(SELECT_BASE + "WHERE UPPER(p.status) = 'ACTIVE' ");
        List<Object> params = new ArrayList<>();

        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append("AND (LOWER(p.name) LIKE ? OR LOWER(p.description) LIKE ?) ");
            String term = "%" + keyword.trim().toLowerCase() + "%";
            params.add(term);
            params.add(term);
        }

        if (category != null && !category.trim().isEmpty() && !category.equalsIgnoreCase("ALL")) {
            sql.append("AND LOWER(p.category) = LOWER(?) ");
            params.add(category.trim());
        }

        sql.append("ORDER BY p.id DESC");
        return executeQueryForList(sql.toString(), params.toArray());
    }

    @Override
    public void updateStock(Long productId, int newStock) {
        String sql = "UPDATE products SET stock = ? WHERE id = ?";
        executeUpdate(sql, Math.max(0, newStock), productId);
    }

    @Override
    public void updateStatus(Long productId, String status) {
        String sql = "UPDATE products SET status = ? WHERE id = ?";
        executeUpdate(sql, status, productId);
    }

    @Override
    public Optional<Product> lockProductForUpdate(Connection conn, Long productId) {
        String sql = "SELECT * FROM products WHERE id = ? FOR UPDATE";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, productId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSet(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to lock product row (ID: " + productId + ") FOR UPDATE: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    @Override
    public Product save(Product product) {
        String sql = "INSERT INTO products (name, description, price, stock, category, image_url, status, seller_id, created_at) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?) RETURNING id";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            setParameters(stmt,
                    product.getName(),
                    product.getDescription(),
                    product.getPrice(),
                    product.getStock(),
                    product.getCategory(),
                    product.getImageUrl(),
                    product.getStatus(),
                    product.getSellerId(),
                    product.getCreatedAt() != null ? product.getCreatedAt() : java.time.LocalDateTime.now()
            );

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    product.setId(rs.getLong("id"));
                }
            }
            return product;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to insert product: " + e.getMessage(), e);
        }
    }

    @Override
    public void update(Product product) {
        String sql = "UPDATE products SET name = ?, description = ?, price = ?, stock = ?, category = ?, image_url = ?, status = ? WHERE id = ?";
        executeUpdate(sql,
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getStock(),
                product.getCategory(),
                product.getImageUrl(),
                product.getStatus(),
                product.getId()
        );
    }

    @Override
    public boolean deleteById(Long id) {
        String sql = "DELETE FROM products WHERE id = ?";
        return executeUpdate(sql, id) > 0;
    }
}
