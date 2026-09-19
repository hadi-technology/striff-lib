package com.hadi.striff;

import com.hadi.clarpse.compiler.CompileException;

import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * Parses the base and head revisions of one language concurrently, on threads this class owns.
 *
 * <p>The threads are owned here so that cancellation reaches the parsers. A parser checks the
 * interrupt flag of the thread it runs on, but a caller enforcing a time budget can only interrupt
 * the thread that is waiting for the result. So the wait is interruptible, and an interrupt is
 * forwarded to both parse threads, where the parsers' own cancellation checks can see it.</p>
 *
 * <p>No parse outlives the call. Whatever ends the wait, whether both results, the first failure or
 * an interrupt, the other parse is cancelled and the call returns only after both parses have
 * ended. A caller that sees this method return can rely on the parse work having stopped.</p>
 *
 * <p>A result that holds resources is never lost: when the call fails, whatever result one side did
 * produce is handed to the caller's discard action before the failure is thrown.</p>
 */
final class ParallelParse {

    private static final AtomicInteger THREAD_COUNT = new AtomicInteger();

    private static final ThreadFactory THREADS = runnable -> {
        Thread thread = new Thread(runnable, "striff-parse-" + THREAD_COUNT.incrementAndGet());
        thread.setDaemon(true);
        return thread;
    };

    private ParallelParse() {
    }

    /**
     * The two results, by revision.
     *
     * @param base result for the base revision
     * @param head result for the head revision
     * @param <T>  the result type
     */
    record Results<T>(T base, T head) {
    }

    /**
     * Runs both parses concurrently and returns their results.
     *
     * @param base parses the base revision
     * @param head parses the head revision
     * @param <T>  the result type
     * @return both results
     * @throws CompileException      if either parse fails with one; the other parse is cancelled
     * @throws CancellationException if the calling thread is interrupted while waiting; both parses
     *                               are cancelled and the interrupt flag is re-asserted
     */
    static <T> Results<T> run(Callable<T> base, Callable<T> head) throws CompileException {
        return run(base, head, result -> { });
    }

    /**
     * Runs both parses concurrently and returns their results, discarding a produced result when
     * the call fails.
     *
     * @param base    parses the base revision
     * @param head    parses the head revision
     * @param discard receives each result that was produced when the call does not return both,
     *                for example to close it
     * @param <T>     the result type
     * @return both results
     * @throws CompileException      if either parse fails with one; the other parse is cancelled
     * @throws CancellationException if the calling thread is interrupted while waiting; both parses
     *                               are cancelled and the interrupt flag is re-asserted
     */
    @SuppressWarnings("PMD.CloseResource")
    static <T> Results<T> run(Callable<T> base, Callable<T> head, Consumer<? super T> discard)
            throws CompileException {
        // Not try-with-resources: ExecutorService is AutoCloseable only from Java 19 and this library
        // targets Java 17. stop(pool) in the finally shuts the pool down and waits for it instead.
        ExecutorService pool = Executors.newFixedThreadPool(2, THREADS);
        boolean interrupted = false;
        boolean returned = false;
        Future<T> baseParse = null;
        Future<T> headParse = null;
        try {
            CompletionService<T> parses = new ExecutorCompletionService<>(pool);
            baseParse = parses.submit(base);
            headParse = parses.submit(head);
            // Completion order, so the first failure ends the wait instead of queueing behind a parse
            // whose result is about to be discarded.
            for (int i = 0; i < 2; i++) {
                resultOf(parses.take());
            }
            Results<T> results = new Results<>(resultOf(baseParse), resultOf(headParse));
            returned = true;
            return results;
        } catch (InterruptedException e) {
            interrupted = true;
            CancellationException cancelled =
                    new CancellationException("Analysis interrupted while parsing source files.");
            cancelled.initCause(e);
            throw cancelled;
        } finally {
            interrupted |= stop(pool);
            if (!returned) {
                discardProduced(baseParse, discard);
                discardProduced(headParse, discard);
            }
            if (interrupted) {
                Thread.currentThread().interrupt();
            }
        }
    }

    /** Hands a parse's result to {@code discard} if the parse produced one. */
    private static <T> void discardProduced(Future<T> parse, Consumer<? super T> discard) {
        if (parse == null || !parse.isDone() || parse.isCancelled()) {
            return;
        }
        try {
            T result = parse.get();
            if (result != null) {
                discard.accept(result);
            }
        } catch (ExecutionException | InterruptedException | CancellationException e) {
            // Nothing was produced, so nothing is held.
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private static <T> T resultOf(Future<T> parse)
            throws InterruptedException, CompileException {
        try {
            return parse.get();
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof CompileException compileException) {
                throw compileException;
            }
            if (cause instanceof RuntimeException runtimeException) {
                throw runtimeException;
            }
            if (cause instanceof Error error) {
                throw error;
            }
            throw new RuntimeException(cause);
        }
    }

    /**
     * Interrupts whatever is still running and waits until neither parse is running.
     *
     * <p>The wait does not give up. A parse that ignores its interrupt runs to completion, as it would
     * have without cancellation, and returning before it does would tell the caller the work had
     * stopped when it had not.</p>
     *
     * @return whether the calling thread was interrupted during the wait, so the flag is restored
     *         rather than lost
     */
    private static boolean stop(ExecutorService pool) {
        pool.shutdownNow();
        boolean interrupted = false;
        boolean terminated = false;
        while (!terminated) {
            try {
                terminated = pool.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
            } catch (InterruptedException e) {
                interrupted = true;
            }
        }
        return interrupted;
    }
}
