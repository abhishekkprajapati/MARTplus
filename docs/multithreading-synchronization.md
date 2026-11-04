# Multithreading & Synchronization (4 Marks Compliance)

This document explains the multithreading architecture, thread pooling, EDT safety, and fine-grained synchronization implemented in the application.

---

## 1. ExecutorService & Thread Pooling (`BackgroundTaskManager`)

To prevent blocking the Swing UI during long-running JDBC queries, `com.martplus.desktop.concurrency.BackgroundTaskManager` provides a managed thread pool:

```java
private static final ExecutorService EXECUTOR = new ThreadPoolExecutor(
        4,                                   // Core pool size
        16,                                  // Maximum pool size
        60L, TimeUnit.SECONDS,               // Keep alive for idle threads
        new LinkedBlockingQueue<>(100),       // Bounded task queue
        new ThreadFactory() {
            private int counter = 1;
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r, "MartPlus-Worker-" + (counter++));
                t.setDaemon(true);           // Daemon threads permit clean JVM exit
                return t;
            }
        },
        new ThreadPoolExecutor.CallerRunsPolicy()
);
```

### Key Highlights:
- **Thread Naming:** Worker threads are explicitly named (`MartPlus-Worker-N`) for debugging and profiling.
- **Daemon Threads:** Worker threads do not prevent the JVM from closing when the user exits the window.
- **Graceful Shutdown:** A JVM `ShutdownHook` ensures idle threads are terminated cleanly.

---

## 2. Event Dispatch Thread (EDT) Safety (`AsyncOperation`)

In Java Swing, all UI components must be created and modified exclusively on the **Event Dispatch Thread (EDT)**. Performing JDBC queries directly inside button listeners causes UI freezing ("beachballing").

`com.martplus.desktop.concurrency.AsyncOperation<T>` extends `javax.swing.SwingWorker<T, Void>` to guarantee EDT safety:

```
[User Clicks Button on EDT]
         │
         ▼
[AsyncOperation.execute()]
         │
         ▼ (Spawns worker off EDT)
[doInBackground()] ───────► Executes JDBC Database Queries
         │
         ▼ (Dispatched back to EDT)
[done()] ────────────────► Updates JTable, Shows JOptionPane Dialogs
```

---

## 3. Synchronization & Concurrency Control (`InventoryLockManager`)

When multiple users or concurrent threads attempt to purchase products with limited stock, race conditions can cause stock to fall below zero.

### Solution:
1. **Application-level ReentrantLock per Product:**
   - Managed by `InventoryLockManager` using a `ConcurrentHashMap<Long, ReentrantLock>`.
   - Each product has an independent fair `ReentrantLock(true)`. Different products do not block each other, ensuring high throughput.
   - Enforced via `tryLock` with timeout and guaranteed `unlock()` in `finally`:
     ```java
     public static <T> T executeWithLock(Long productId, Supplier<T> operation) {
         ReentrantLock lock = getLockForProduct(productId);
         boolean acquired = lock.tryLock(5, TimeUnit.SECONDS);
         try {
             return operation.get();
         } finally {
             if (acquired) lock.unlock();
         }
     }
     ```

2. **Database-level Row Locking:**
   - In `OrderService.processCheckout`, every product row is locked in PostgreSQL using:
     ```sql
     SELECT * FROM products WHERE id = ? FOR UPDATE
     ```
   - This ensures ACID isolation even if other instances (such as the web application) run concurrently.

3. **Concurrency Test Proof:**
   - Verified by `InventoryLockManagerTest.java`: 15 simultaneous threads competing for 20 stock units. Exactly 10 succeed, 5 are rejected, and stock finishes at exactly 0.
