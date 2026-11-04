package com.martplus.desktop.dao;

import com.martplus.desktop.model.Order;
import com.martplus.desktop.model.OrderItem;
import com.martplus.desktop.model.OrderTracking;

import java.sql.Connection;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object contract for Orders, Order Items, and Tracking.
 */
public interface OrderDao extends GenericDao<Order, Long> {

    List<Order> findByBuyerId(Long buyerId);

    List<Order> findOrdersForSeller(Long sellerId);

    List<Order> findByStatus(String status);

    List<OrderItem> findItemsByOrderId(Long orderId);

    List<OrderItem> findItemsByOrderIdAndSellerId(Long orderId, Long sellerId);

    List<OrderTracking> findTrackingByOrderId(Long orderId);

    void updateOrderStatus(Long orderId, String newStatus);

    // Transaction-aware methods
    Order insertOrder(Connection conn, Order order);

    void insertOrderItems(Connection conn, List<OrderItem> items);

    void insertTracking(Connection conn, OrderTracking tracking);
}
