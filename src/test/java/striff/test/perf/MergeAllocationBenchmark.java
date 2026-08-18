package striff.test.perf;

import com.hadi.clarpse.sourcemodel.OOPSourceCodeModel;
import com.hadi.striff.parse.CodeDiff;

import com.sun.management.ThreadMXBean;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Measures what one two-revision merge allocates, so a change to it can be judged rather than
 * asserted.
 *
 * <p><b>Metric.</b> {@code ThreadMXBean.getThreadAllocatedBytes} on the calling thread, which counts
 * every byte the thread allocated in the Java heap whether or not it survived. It is used in
 * preference to wall clock or {@code Runtime.freeMemory()} because it is exact and does not depend
 * on when a collection happened to run -- the same merge reports the same figure to within a
 * fraction of a percent across runs, where free-memory deltas move by whole percentages.
 *
 * <p>Allocation rather than live size is the primary number because of how the failure presented: a
 * container was OOMKilled with {@code -Xmx} at 4g, no {@code OutOfMemoryError}, and roughly 6GB
 * outside every {@code jvm_memory_*} pool. Heap exhaustion does not do that; allocation churn and
 * the collector bookkeeping it drives does.
 *
 * <p><b>Retained size</b> is reported alongside it, as a used-heap reading taken after repeated
 * {@code System.gc()} with only the models reachable. It is the weaker of the two numbers -- a
 * collection is a request, not a guarantee -- so it is quoted to two significant figures and never
 * used on its own.
 *
 * <p>Run it with {@code -Xmx} set well above the working set, so that no measured iteration competes
 * with a collector that is short of room.
 */
public final class MergeAllocationBenchmark {

    private static final int WARMUP_ITERATIONS = 1;
    private static final int MEASURED_ITERATIONS = 3;
    private static final double MEBIBYTE = 1024.0 * 1024.0;

    private MergeAllocationBenchmark() {
    }

    /**
     * Runs the benchmark and prints one line per iteration plus a summary.
     *
     * @param args Ignored.
     */
    public static void main(final String[] args) {
        ThreadMXBean threads = (ThreadMXBean) ManagementFactory.getThreadMXBean();
        if (!threads.isThreadAllocatedMemorySupported()) {
            throw new IllegalStateException("Thread allocation counters unavailable on this JVM.");
        }
        threads.setThreadAllocatedMemoryEnabled(true);

        OOPSourceCodeModel base = SyntheticModels.baseRevision();
        OOPSourceCodeModel head = SyntheticModels.headRevision();
        System.out.printf(Locale.ROOT, "model sizes: base=%d components, head=%d components%n",
                base.size(), head.size());
        System.out.printf(Locale.ROOT, "retained: base+head models = %.0f MiB%n",
                retainedMib(base, head));

        long baseSizeBefore = base.size();
        long headSizeBefore = head.size();

        for (int i = 0; i < WARMUP_ITERATIONS; i++) {
            consume(new CodeDiff(base, head));
        }

        List<Long> allocations = new ArrayList<>();
        CodeDiff last = null;
        for (int i = 0; i < MEASURED_ITERATIONS; i++) {
            long before = threads.getCurrentThreadAllocatedBytes();
            CodeDiff diff = new CodeDiff(base, head);
            long allocated = threads.getCurrentThreadAllocatedBytes() - before;
            allocations.add(allocated);
            last = diff;
            System.out.printf(Locale.ROOT, "iteration %d: allocated %,d bytes (%.1f MiB)%n",
                    i + 1, allocated, allocated / MEBIBYTE);
        }

        if (base.size() != baseSizeBefore || head.size() != headSizeBefore) {
            throw new IllegalStateException("The merge mutated one of its input models.");
        }

        long min = allocations.stream().mapToLong(Long::longValue).min().orElseThrow();
        long max = allocations.stream().mapToLong(Long::longValue).max().orElseThrow();
        long median = allocations.stream().sorted().toList().get(allocations.size() / 2);
        System.out.printf(Locale.ROOT,
                "MERGE ALLOCATION median %,d bytes (%.1f MiB), min %.1f MiB, max %.1f MiB%n",
                median, median / MEBIBYTE, min / MEBIBYTE, max / MEBIBYTE);
        System.out.printf(Locale.ROOT, "merged model: %d components%n", last.mergedModel().size());
        System.out.printf(Locale.ROOT, "changeset: +%d -%d ~%d components%n",
                last.changeSet().addedComponents().size(),
                last.changeSet().deletedComponents().size(),
                last.changeSet().modifiedComponents().size());
        System.out.printf(Locale.ROOT, "retained: base+head+diff = %.0f MiB%n",
                retainedMib(base, head, last));
    }

    private static double retainedMib(final Object... keepReachable) {
        MemoryMXBean memory = ManagementFactory.getMemoryMXBean();
        for (int i = 0; i < 6; i++) {
            System.gc();
            try {
                Thread.sleep(60);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException(e);
            }
        }
        long used = memory.getHeapMemoryUsage().getUsed();
        consume(keepReachable);
        return used / MEBIBYTE;
    }

    @SuppressWarnings("unused")
    private static void consume(final Object value) {
        if (value == null) {
            throw new IllegalStateException("null result");
        }
    }
}
