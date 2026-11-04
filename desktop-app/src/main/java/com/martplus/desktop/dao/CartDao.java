package com.martplus.desktop.dao;

import com.martplus.desktop.model.CartItem;

import java.sql.Connection;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object contract for Shopping Cart operations.
 */
public interface CartDao extends GenericDao<CartItem, Long> {

    List<CartItem> findByBuyerId(Long buyerId);

    Optional<CartItem> findByBuyerIdAndProductId(Long buyerId, Long productId);

    void updateQuantity(Long cartItemId, int quantity);

    void deleteByBuyerId(Long buyerId);

    /**
     * Clears cart items within an active database transaction.
     */
    void deleteByBuyerId(Connection conn, Long buyerId);

    int countByBuyerId(Long buyerId);
}
