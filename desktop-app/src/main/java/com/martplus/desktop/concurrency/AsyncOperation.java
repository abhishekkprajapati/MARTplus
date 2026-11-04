package com.martplus.desktop.concurrency;

import javax.swing.*;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

/**
 * Reusable SwingWorker abstraction to execute database operations off the EDT
 * and safely dispatch UI updates, loading spinners, and error handlers back to the EDT.
 *
 * @param <T> Return type of the background operation.
 */
public class AsyncOperation<T> extends SwingWorker<T, Void> {

    private final Callable<T> backgroundTask;
    private final Consumer<T> onSuccess;
    private final Consumer<Throwable> onError;
    private final Runnable onFinally;

    public AsyncOperation(Callable<T> backgroundTask,
                          Consumer<T> onSuccess,
                          Consumer<Throwable> onError,
                          Runnable onFinally) {
        this.backgroundTask = backgroundTask;
        this.onSuccess = onSuccess;
        this.onError = onError;
        this.onFinally = onFinally;
    }

    public static <R> AsyncOperation<R> run(Callable<R> task,
                                            Consumer<R> onSuccess,
                                            Consumer<Throwable> onError,
                                            Runnable onFinally) {
        AsyncOperation<R> worker = new AsyncOperation<>(task, onSuccess, onError, onFinally);
        worker.execute();
        return worker;
    }

    public static <R> AsyncOperation<R> run(Callable<R> task,
                                            Consumer<R> onSuccess,
                                            Consumer<Throwable> onError) {
        return run(task, onSuccess, onError, null);
    }

    @Override
    protected T doInBackground() throws Exception {
        return backgroundTask.call();
    }

    @Override
    protected void done() {
        try {
            if (!isCancelled()) {
                T result = get();
                if (onSuccess != null) {
                    onSuccess.accept(result);
                }
            }
        } catch (Throwable t) {
            Throwable cause = t.getCause() != null ? t.getCause() : t;
            if (onError != null) {
                onError.accept(cause);
            }
        } finally {
            if (onFinally != null) {
                onFinally.run();
            }
        }
    }
}
