package com.martplus.desktop.dao;

import com.martplus.desktop.exception.DatabaseException;
import com.martplus.desktop.model.Order;
import com.martplus.desktop.model.OrderItem;
import com.martplus.desktop.model.OrderTracking;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * JDBC implementation of OrderDao for PostgreSQL 'orders', 'order_items', and 'order_tracking' tables.
 */
public class JdbcOrderDao extends AbstractJdbcDao<Order, Long> implements OrderDao {

    @Override
    protected Order mapResultSet(ResultSet rs) throws SQLException {
        Timestamp createdAt = rs.getTimestamp("created_at");
        Timestamp updatedAt = rs.getTimestamp("updated_at");

        Order order = new Order(
                rs.getLong("id"),
                rs.getLong("buyer_id"),
                rs.getObject("shipping_address_id") != null ? rs.getLong("shipping_address_id") : null,
                rs.getBigDecimal("total_amount"),
                rs.getString("status"),
                rs.getString("payment_status"),
                rs.getString("payment_method"),
                createdAt != null ? createdAt.toLocalDateTime() : null,
                updatedAt != null ? updatedAt.toLocalDateTime() : null
        );

        try {
            String bFirst = rs.getString("buyer_first_name");
            String bLast = rs.getString("buyer_last_name");
            if (bFirst != null || bLast != null) {
                order.setBuyerName(((bFirst != null ? bFirst : "") + " " + (bLast != null ? bLast : "")).trim());
            }
            order.setBuyerEmail(rs.getString("buyer_email"));
        } catch (SQLException ignored) {
        }

        try {
            String addrLine = rs.getString("addr_line");
            String city = rs.getString("addr_city");
            if (addrLine != null) {
                order.setShippingAddressText(addrLine + ", " + city);
            }
        } catch (SQLException ignored) {
        }

        return order;
    }

    private static final String SELECT_BASE =
            "SELECT o.*, u.first_name AS buyer_first_name, u.last_name AS buyer_last_name, u.email AS buyer_email, " +
            "a.address_line AS addr_line, a.city AS addr_city " +
            "FROM orders o " +
            "LEFT JOIN users u ON o.buyer_id = u.id " +
            "LEFT JOIN addresses a ON o.shipping_address_id = a.id ";

    @Override
    public Optional<Order> findById(Long id) {
        String sql = SELECT_BASE + "WHERE o.id = ?";
        return executeQueryForSingle(sql, id);
    }

    @Override
    public List<Order> findAll() {
        String sql = SELECT_BASE + "ORDER BY o.id DESC";
        return executeQueryForList(sql);
    }

    @Override
    public List<Order> findByBuyerId(Long buyerId) {
        String sql = SELECT_BASE + "WHERE o.buyer_id = ? ORDER BY o.id DESC";
        return executeQueryForList(sql, buyerId);
    }

    @Override
    public List<Order> findOrdersForSeller(Long sellerId) {
        String sql = SELECT_BASE +
                     "WHERE o.id IN (SELECT DISTINCT oi.order_id FROM order_items oi JOIN products p ON oi.product_id = p.id WHERE p.seller_id = ?) " +
                     "ORDER BY o.id DESC";
        return executeQueryForList(sql, sellerId);
    }

    @Override
    public List<Order> findByStatus(String status) {
        if (status == null || status.isBlank() || status.equalsIgnoreCase("ALL")) {
            return findAll();
        }
        String sql = SELECT_BASE + "WHERE UPPER(o.status) = UPPER(?) ORDER BY o.id DESC";
        return executeQueryForList(sql, status.trim());
    }

    @Override
    public List<OrderItem> findItemsByOrderId(Long orderId) {
        String sql = "SELECT oi.*, p.name AS product_name, p.category AS product_category, p.seller_id " +
                     "FROM order_items oi " +
                     "JOIN products p ON oi.product_id = p.id " +
                     "WHERE oi.order_id = ? ORDER BY oi.id ASC";
        List<OrderItem> items = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, orderId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    OrderItem item = new OrderItem(
                            rs.getLong("id"),
                            rs.getLong("order_id"),
                            rs.getLong("product_id"),
                            rs.getInt("quantity"),
                            rs.getBigDecimal("price"),
                            rs.getBigDecimal("subtotal")
                    );
                    item.setProductName(rs.getString("product_name"));
                    item.setProductCategory(rs.getString("product_category"));
                    item.setSellerId(rs.getObject("seller_id") != null ? rs.getLong("seller_id") : null);
                    items.add(item);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to find order items: " + e.getMessage(), e);
        }
        return items;
    }

    @Override
    public List<OrderItem> findItemsByOrderIdAndSellerId(Long orderId, Long sellerId) {
        String sql = "SELECT oi.*, p.name AS product_name, p.category AS product_category, p.seller_id " +
                     "FROM order_items oi " +
                     "JOIN products p ON oi.product_id = p.id " +
                     "WHERE oi.order_id = ? AND p.seller_id = ? ORDER BY oi.id ASC";
        List<OrderItem> items = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, orderId);
            stmt.setLong(2, sellerId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    OrderItem item = new OrderItem(
                            rs.getLong("id"),
                            rs.getLong("order_id"),
                            rs.getLong("product_id"),
                            rs.getInt("quantity"),
                            rs.getBigDecimal("price"),
                            rs.getBigDecimal("subtotal")
                    );
                    item.setProductName(rs.getString("product_name"));
                    item.setProductCategory(rs.getString("product_category"));
                    item.setSellerId(sellerId);
                    items.add(item);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to find seller order items: " + e.getMessage(), e);
        }
        return items;
    }

    @Override
    public List<OrderTracking> findTrackingByOrderId(Long orderId) {
        String sql = "SELECT * FROM order_tracking WHERE order_id = ? ORDER BY created_at ASC, id ASC";
        List<OrderTracking> list = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, orderId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Timestamp ts = rs.getTimestamp("created_at");
                    list.add(new OrderTracking(
                            rs.getLong("id"),
                            rs.getLong("order_id"),
                            rs.getString("status"),
                            rs.getString("description"),
                            ts != null ? ts.toLocalDateTime() : null
                    ));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to fetch order tracking: " + e.getMessage(), e);
        }
        return list;
    }

    @Override
    public void updateOrderStatus(Long orderId, String newStatus) {
        String sql = "UPDATE orders SET status = ?, updated_at = ? WHERE id = ?";
        executeUpdate(sql, newStatus, java.time.LocalDateTime.now(), orderId);

        // Also append tracking record
        String trackSql = "INSERT INTO order_tracking (order_id, status, description, created_at) VALUES (?, ?, ?, ?)";
        executeUpdate(trackSql, orderId, newStatus, "Status updated to " + newStatus, java.time.LocalDateTime.now());
    }

    @Override
    public Order insertOrder(Connection conn, Order order) {
        String sql = "INSERT INTO orders (buyer_id, shipping_address_id, total_amount, status, payment_status, payment_method, created_at, updated_at) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?) RETURNING id";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            java.time.LocalDateTime now = java.time.LocalDateTime.now();
            stmt.setLong(1, order.getBuyerId());
            if (order.getShippingAddressId() != null) {
                stmt.setLong(2, order.getShippingAddressId());
            } else {
                stmt.setNull(2, Types.BIGINT);
            }
            stmt.setBigDecimal(3, order.getTotalAmount());
            stmt.setString(4, order.getStatus());
            stmt.setString(5, order.getPaymentStatus());
            stmt.setString(6, order.getPaymentMethod());
            stmt.setTimestamp(7, Timestamp.valueOf(order.getCreatedAt() != null ? order.getCreatedAt() : now));
            stmt.setTimestamp(8, Timestamp.valueOf(now));

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    order.setId(rs.getLong("id"));
                }
            }
            return order;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to insert order in transaction: " + e.getMessage(), e);
        }
    }

    @Override
    public void insertOrderItems(Connection conn, List<OrderItem> items) {
        String sql = "INSERT INTO order_items (order_id, product_id, quantity, price, subtotal) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (OrderItem item : items) {
                stmt.setLong(1, item.getOrderId());
                stmt.setLong(2, item.getProductId());
                stmt.setInt(3, item.getQuantity());
                stmt.setBigDecimal(4, item.getPrice());
                stmt.setBigDecimal(5, item.getSubtotal());
                stmt.addBatch();
            }
            stmt.executeBatch();
        } catch (SQLException e) {
            throw new DatabaseException("Failed to insert order items in batch: " + e.getMessage(), e);
        }
    }

    @Override
    public void insertTracking(Connection conn, OrderTracking tracking) {
        String sql = "INSERT INTO order_tracking (order_id, status, description, created_at) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, tracking.getOrderId());
            stmt.setString(2, tracking.getStatus());
            stmt.setString(3, tracking.getDescription());
            stmt.setTimestamp(4, Timestamp.valueOf(tracking.getCreatedAt() != null ? tracking.getCreatedAt() : java.time.LocalDateTime.now()));
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Failed to insert tracking milestone: " + e.getMessage(), e);
        }
    }

    @Override
    public Order save(Order order) {
        try (Connection conn = getConnection()) {
            return insertOrder(conn, order);
        } catch (SQLException e) {
            throw new DatabaseException("Failed to save order: " + e.getMessage(), e);
        }
    }

    @Override
    public void update(Order order) {
        String sql = "UPDATE orders SET status = ?, payment_status = ?, updated_at = ? WHERE id = ?";
        executeUpdate(sql, order.getStatus(), order.getPaymentStatus(), java.time.LocalDateTime.now(), order.getId());
    }

    @Override
    public boolean deleteById(Long id) {
        String sql = "DELETE FROM orders WHERE id = ?";
        return executeUpdate(sql, id) > 0;
    }
}
