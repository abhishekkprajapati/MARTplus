package com.martplus.desktop.service;

import com.martplus.desktop.concurrency.InventoryLockManager;
import com.martplus.desktop.dao.InventoryDao;
import com.martplus.desktop.dao.ProductDao;
import com.martplus.desktop.exception.ValidationException;
import com.martplus.desktop.model.Product;
import com.martplus.desktop.util.ValidationUtil;

import java.util.List;

/**
 * Service managing inventory stock, synchronized updates, and low-stock alerts.
 * Demonstrates:
 * 1. Thread Synchronization via InventoryLockManager.
 * 2. Prevention of negative inventory balances.
 */
public class InventoryService {

    private final InventoryDao inventoryDao;
    private final ProductDao productDao;

    public InventoryService(InventoryDao inventoryDao, ProductDao productDao) {
        this.inventoryDao = inventoryDao;
        this.productDao = productDao;
    }

    public int getStock(Long productId) {
        return inventoryDao.getStock(productId);
    }

    public void updateStock(Long productId, int newStock) {
        ValidationUtil.validateNonNegativeStock(newStock, "Stock");

        // Synchronize using ReentrantLock per-product to ensure thread safety
        InventoryLockManager.runWithLock(productId, () -> {
            inventoryDao.updateStock(productId, newStock);
        });
    }

    public void adjustStock(Long productId, int quantityDelta) {
        InventoryLockManager.runWithLock(productId, () -> {
            int currentStock = inventoryDao.getStock(productId);
            int newStock = currentStock + quantityDelta;
            if (newStock < 0) {
                throw new ValidationException("Cannot reduce stock by " + Math.abs(quantityDelta) +
                        ". Current available stock is only " + currentStock);
            }
            inventoryDao.updateStock(productId, newStock);
        });
    }

    public List<Product> getLowStockAlerts(Long sellerId, int threshold) {
        return inventoryDao.findLowStockProducts(sellerId, threshold);
    }
}
