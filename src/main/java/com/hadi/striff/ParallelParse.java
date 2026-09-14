package com.hadi.striff;

import com.hadi.clarpse.compiler.CompileException;
import com.hadi.clarpse.compiler.CompileResult;

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

/**
 * Parses the base and head revisions of one language concurrently, on threads this class owns.
 *
 * <p>The threads are owned here so that cancellation reaches the parsers. A parser checks the
 * interrupt flag of the thread it runs on, but a caller enforcing a time budget can only interrupt
 * the thread that is waiting for the result. So the wait is interruptible, and an interrupt is
 * forwarded to both parse threads, where the parsers' own cancellation checks can see it.</p>
 *
 * <p>No parse outlives the call. Whatever ends the wait, whether both results, the first failure or
 * an interrupt, the other parse is cancelled and the call returns only after both threads have
 * stopped. A caller that sees this method return can rely on the parse work having ended.</p>
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
     * The two parse results, by revision.
     *
     * @param base result for the base revision
     * @param head result for the head revision
     */
    record Results(CompileResult base, CompileResult head) {
    }

    /**
     * Runs both parses concurrently and returns their results.
     *
     * @param base parses the base revision
     * @param head parses the head revision
     * @return both results
     * @throws CompileException      if either parse fails with one; the other parse is cancelled
     * @throws CancellationException if the calling thread is interrupted while waiting; both parses
     *                               are cancelled and the interrupt flag is re-asserted
     */
    static Results run(Callable<CompileResult> base, Callable<CompileResult> head)
            throws CompileException {
        ExecutorService pool = Executors.newFixedThreadPool(2, THREADS);
        boolean interrupted = false;
        try {
            CompletionService<CompileResult> parses = new ExecutorCompletionService<>(pool);
            Future<CompileResult> baseParse = parses.submit(base);
            Future<CompileResult> headParse = parses.submit(head);
            // Completion order, so the first failure ends the wait instead of queueing behind a parse
            // whose result is about to be discarded.
            for (int i = 0; i < 2; i++) {
                resultOf(parses.take());
            }
            return new Results(resultOf(baseParse), resultOf(headParse));
        } catch (InterruptedException e) {
            interrupted = true;
            CancellationException cancelled =
                    new CancellationException("Analysis interrupted while parsing source files.");
            cancelled.initCause(e);
            throw cancelled;
        } finally {
            interrupted |= stop(pool);
            if (interrupted) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private static CompileResult resultOf(Future<CompileResult> parse)
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
     * Interrupts whatever is still running and waits for both threads to end.
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
