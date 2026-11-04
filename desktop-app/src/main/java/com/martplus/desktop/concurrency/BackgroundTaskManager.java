package com.martplus.desktop.concurrency;

import java.util.concurrent.*;

/**
 * Manages thread pools for background operations outside the Swing Event Dispatch Thread (EDT).
 * Demonstrates:
 * 1. Multithreading: Custom ExecutorService.
 * 2. Proper thread lifecycle, daemon thread factories, and graceful shutdown.
 */
public final class BackgroundTaskManager {

    private static final int CORE_POOL_SIZE = 4;
    private static final int MAX_POOL_SIZE = 16;
    private static final long KEEP_ALIVE_TIME_SECONDS = 60L;

    private static final ExecutorService EXECUTOR = new ThreadPoolExecutor(
            CORE_POOL_SIZE,
            MAX_POOL_SIZE,
            KEEP_ALIVE_TIME_SECONDS,
            TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(100),
            new ThreadFactory() {
                private int counter = 1;
                @Override
                public Thread newThread(Runnable r) {
                    Thread t = new Thread(r, "MartPlus-Worker-" + (counter++));
                    t.setDaemon(true);
                    return t;
                }
            },
            new ThreadPoolExecutor.CallerRunsPolicy()
    );

    static {
        Runtime.getRuntime().addShutdownHook(new Thread(BackgroundTaskManager::shutdown, "MartPlus-ShutdownHook"));
    }

    private BackgroundTaskManager() {
    }

    /**
     * Submits a background Runnable task to the thread pool.
     */
    public static Future<?> submit(Runnable task) {
        return EXECUTOR.submit(task);
    }

    /**
     * Submits a background Callable task to the thread pool.
     */
    public static <T> Future<T> submit(Callable<T> task) {
        return EXECUTOR.submit(task);
    }

    /**
     * Gracefully shuts down the executor pool.
     */
    public static void shutdown() {
        EXECUTOR.shutdown();
        try {
            if (!EXECUTOR.awaitTermination(2, TimeUnit.SECONDS)) {
                EXECUTOR.shutdownNow();
            }
        } catch (InterruptedException e) {
            EXECUTOR.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
