package com.martplus.desktop.concurrency;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

/**
 * Thread-safe coordination manager for concurrent inventory stock operations.
 * Demonstrates:
 * 1. Java Synchronization: ReentrantLock and explicit critical sections.
 * 2. Concurrent Collections: ConcurrentHashMap for fine-grained per-product locks.
 * 3. Deadlock prevention: Lock acquisition timeouts and try/finally release guarantees.
 */
public final class InventoryLockManager {

    private static final ConcurrentHashMap<Long, ReentrantLock> PRODUCT_LOCKS = new ConcurrentHashMap<>();
    private static final long LOCK_TIMEOUT_SECONDS = 5L;

    private InventoryLockManager() {
    }

    /**
     * Obtains or computes the ReentrantLock associated with a specific product ID.
     */
    private static ReentrantLock getLockForProduct(Long productId) {
        return PRODUCT_LOCKS.computeIfAbsent(productId, k -> new ReentrantLock(true)); // Fair locking
    }

    /**
     * Executes a supplier operation within a synchronized, reentrant lock critical section for a specific product.
     *
     * @param productId Product identifier to coordinate.
     * @param operation Action to perform inside the locked section.
     * @param <T> Return type.
     * @return Result of the operation.
     */
    public static <T> T executeWithLock(Long productId, Supplier<T> operation) {
        ReentrantLock lock = getLockForProduct(productId);
        boolean acquired = false;
        try {
            acquired = lock.tryLock(LOCK_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            if (!acquired) {
                throw new IllegalStateException("Timeout acquiring inventory lock for product ID: " + productId);
            }
            return operation.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while waiting for inventory lock on product " + productId, e);
        } finally {
            if (acquired) {
                lock.unlock();
            }
        }
    }

    /**
     * Executes a runnable action within a synchronized critical section for a specific product.
     */
    public static void runWithLock(Long productId, Runnable action) {
        executeWithLock(productId, () -> {
            action.run();
            return null;
        });
    }

    /**
     * Cleans up locks when product count grows excessively.
     */
    public static void clearUnusedLocks() {
        PRODUCT_LOCKS.entrySet().removeIf(entry -> !entry.getValue().isLocked() && !entry.getValue().hasQueuedThreads());
    }
}
