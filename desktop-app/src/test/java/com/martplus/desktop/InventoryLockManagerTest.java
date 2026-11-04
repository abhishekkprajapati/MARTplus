package com.martplus.desktop;

import com.martplus.desktop.concurrency.InventoryLockManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Multithreading & Inventory Synchronization Tests")
public class InventoryLockManagerTest {

    @Test
    @DisplayName("Should prevent race conditions: stock never goes below zero under simultaneous purchase attempts")
    void testConcurrentInventoryDeductionNeverBelowZero() throws InterruptedException {
        final Long testProductId = 101L;
        final int initialStock = 20;
        final AtomicInteger sharedStock = new AtomicInteger(initialStock);
        final AtomicInteger successfulPurchases = new AtomicInteger(0);
        final AtomicInteger rejectedPurchases = new AtomicInteger(0);

        final int totalThreads = 15;
        final int deductionPerThread = 2; // 15 threads * 2 units = 30 total attempted (initial is 20)

        ExecutorService executor = Executors.newFixedThreadPool(totalThreads);
        CountDownLatch latch = new CountDownLatch(totalThreads);

        for (int i = 0; i < totalThreads; i++) {
            executor.submit(() -> {
                try {
                    // Synchronize critical section using InventoryLockManager ReentrantLock
                    InventoryLockManager.runWithLock(testProductId, () -> {
                        int current = sharedStock.get();
                        if (current >= deductionPerThread) {
                            // Simulate small operational delay
                            try {
                                Thread.sleep(5);
                            } catch (InterruptedException ignored) {}

                            sharedStock.addAndGet(-deductionPerThread);
                            successfulPurchases.incrementAndGet();
                        } else {
                            rejectedPurchases.incrementAndGet();
                        }
                    });
                } finally {
                    latch.countDown();
                }
            });
        }

        boolean completed = latch.await(10, TimeUnit.SECONDS);
        executor.shutdown();

        assertTrue(completed, "All concurrent purchase threads should complete within timeout");
        assertTrue(sharedStock.get() >= 0, "Stock must NEVER fall below zero!");
        assertEquals(0, sharedStock.get(), "All 20 available units should have been allocated");
        assertEquals(10, successfulPurchases.get(), "Exactly 10 purchases of 2 units should succeed");
        assertEquals(5, rejectedPurchases.get(), "Remaining 5 purchases must be rejected due to stock exhaustion");
    }
}
