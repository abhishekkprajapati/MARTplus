package com.martplus.desktop.dao;

import com.martplus.desktop.model.Product;

import java.sql.Connection;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object contract for Product catalog operations.
 */
public interface ProductDao extends GenericDao<Product, Long> {

    List<Product> findBySellerId(Long sellerId);

    List<Product> searchByNameAndCategory(String keyword, String category);

    List<Product> findAvailableProducts();

    void updateStock(Long productId, int newStock);

    void updateStatus(Long productId, String status);

    /**
     * Executes SELECT ... FOR UPDATE within an active transaction to lock the product row.
     */
    Optional<Product> lockProductForUpdate(Connection conn, Long productId);
}
