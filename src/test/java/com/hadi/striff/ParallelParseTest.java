package com.hadi.striff;

import com.hadi.clarpse.compiler.CompileException;
import com.hadi.clarpse.compiler.CompileResult;
import com.hadi.clarpse.compiler.Lang;
import com.hadi.clarpse.compiler.ProjectFile;
import com.hadi.clarpse.compiler.ProjectFiles;
import com.hadi.clarpse.sourcemodel.OOPSourceCodeModel;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Cancellation of the concurrent base/head parse.
 *
 * <p>A caller that enforces a time budget interrupts the thread waiting for the parse. These tests pin
 * that the interrupt reaches both parse threads, that the call returns only once both have stopped,
 * that one failed parse stops the other, and that an ordinary run is unaffected.</p>
 */
public class ParallelParseTest {

    private static final long WAIT_SECONDS = 10;

    @Before
    public void clearInterruptBefore() {
        Thread.interrupted();
    }

    @After
    public void clearInterruptAfter() {
        Thread.interrupted();
    }

    private static CompileResult emptyResult() {
        return new CompileResult(new OOPSourceCodeModel());
    }

    /** A parse that signals it has started, then runs until its thread is interrupted. */
    private static final class ParseUntilInterrupted implements Callable<CompileResult> {
        private final CountDownLatch started = new CountDownLatch(1);
        private final CountDownLatch exited = new CountDownLatch(1);
        private volatile boolean interrupted;

        @Override
        public CompileResult call() throws InterruptedException {
            started.countDown();
            try {
                Thread.sleep(Long.MAX_VALUE);
                return emptyResult();
            } catch (InterruptedException e) {
                interrupted = true;
                throw e;
            } finally {
                exited.countDown();
            }
        }

        boolean hasStarted() throws InterruptedException {
            return started.await(WAIT_SECONDS, TimeUnit.SECONDS);
        }

        boolean hasExited() {
            return exited.getCount() == 0;
        }
    }

    @Test(timeout = 60_000)
    public void interruptingTheWaitingThreadStopsBothParses() throws Exception {
        ParseUntilInterrupted base = new ParseUntilInterrupted();
        ParseUntilInterrupted head = new ParseUntilInterrupted();
        AtomicReference<Throwable> thrown = new AtomicReference<>();
        AtomicBoolean flagSetOnThrow = new AtomicBoolean();
        AtomicBoolean bothExitedOnThrow = new AtomicBoolean();

        Thread caller = new Thread(() -> {
            try {
                ParallelParse.run(base, head);
            } catch (Throwable t) {
                thrown.set(t);
                flagSetOnThrow.set(Thread.currentThread().isInterrupted());
                bothExitedOnThrow.set(base.hasExited() && head.hasExited());
            }
        });
        caller.start();
        assertTrue("the base parse must start", base.hasStarted());
        assertTrue("the head parse must start", head.hasStarted());

        caller.interrupt();
        caller.join(TimeUnit.SECONDS.toMillis(WAIT_SECONDS));

        assertFalse("the call must return once its thread is interrupted", caller.isAlive());
        assertTrue("an interrupted parse must surface as a CancellationException, got " + thrown.get(),
                thrown.get() instanceof CancellationException);
        assertTrue("the interrupt flag must be re-asserted before throwing", flagSetOnThrow.get());
        assertTrue("the interrupt must reach the base parse's thread", base.interrupted);
        assertTrue("the interrupt must reach the head parse's thread", head.interrupted);
        assertTrue("the call must not return while a parse is still running", bothExitedOnThrow.get());
    }

    @Test(timeout = 60_000)
    public void aFailedParseStopsTheOtherWithoutWaitingForIt() throws Exception {
        ParseUntilInterrupted base = new ParseUntilInterrupted();
        CompileException failure = new CompileException("head revision failed to parse");
        Callable<CompileResult> head = () -> {
            base.hasStarted();
            throw failure;
        };

        try {
            ParallelParse.run(base, head);
            fail("a failed parse must fail the call");
        } catch (CompileException e) {
            assertSame("the parse's own CompileException must reach the caller unwrapped", failure, e);
        }
        assertTrue("the surviving parse must be interrupted", base.interrupted);
        assertTrue("the call must not return while the surviving parse is still running",
                base.hasExited());
        assertFalse("a parse failure is not a cancellation of the caller",
                Thread.currentThread().isInterrupted());
    }

    @Test
    public void bothResultsAreReturnedByRevisionAndNoParseThreadRemains() throws Exception {
        CompileResult baseResult = emptyResult();
        CompileResult headResult = emptyResult();

        ParallelParse.Results results = ParallelParse.run(() -> baseResult, () -> headResult);

        assertSame(baseResult, results.base());
        assertSame(headResult, results.head());
        assertFalse(Thread.currentThread().isInterrupted());
        assertEquals("no parse thread may outlive the call", 0, liveParseThreads());
    }

    @Test
    public void anInterruptedOperationThrowsCancellationAndLeavesNoParseThread() throws Exception {
        ProjectFiles oldFiles = new ProjectFiles();
        oldFiles.insertFile(new ProjectFile("/ClassA.java",
                "package com.sample; public class ClassA { }"));
        ProjectFiles newFiles = new ProjectFiles();
        newFiles.insertFile(new ProjectFile("/ClassA.java",
                "package com.sample; public class ClassA { private int count; }"));
        StriffConfig config = new StriffConfig().setLanguages(Set.of(Lang.JAVA));

        Thread.currentThread().interrupt();
        try {
            new StriffOperation(oldFiles, newFiles, config);
            fail("an operation started on an interrupted thread must not complete");
        } catch (CancellationException expected) {
            assertTrue("the interrupt flag must remain observable after the throw",
                    Thread.currentThread().isInterrupted());
        }
        assertEquals("no parse thread may outlive the operation", 0, liveParseThreads());
    }

    private static long liveParseThreads() {
        return Thread.getAllStackTraces().keySet().stream()
                .filter(Thread::isAlive)
                .filter(t -> t.getName().startsWith("striff-parse-"))
                .count();
    }
}
