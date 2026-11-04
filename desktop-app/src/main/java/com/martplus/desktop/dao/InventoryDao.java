package com.martplus.desktop.dao;

import com.martplus.desktop.model.Product;

import java.sql.Connection;
import java.util.List;

/**
 * Data Access Object contract for Product Inventory and Stock Management.
 */
public interface InventoryDao {

    int getStock(Long productId);

    void updateStock(Long productId, int newStock);

    /**
     * Deducts quantity from product stock inside an active transaction.
     */
    void deductStock(Connection conn, Long productId, int quantity);

    void addStock(Long productId, int quantity);

    List<Product> findLowStockProducts(Long sellerId, int threshold);
}
