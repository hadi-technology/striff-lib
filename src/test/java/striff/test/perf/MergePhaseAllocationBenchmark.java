package striff.test.perf;

import com.hadi.clarpse.sourcemodel.OOPSourceCodeModel;
import com.hadi.striff.ChangeSet;
import com.hadi.striff.extractor.ExtractedRelationships;
import com.hadi.striff.parse.CodeDiff;
import com.sun.management.ThreadMXBean;

import java.lang.management.ManagementFactory;
import java.util.Locale;

/**
 * Attributes a merge's allocation to its phases, so a total can be acted on rather than just quoted.
 *
 * <p>Each phase is driven through the same public entry point {@link CodeDiff}'s constructor uses,
 * and the merge itself -- which has no public entry point -- is <b>derived</b>, as the whole
 * constructor's allocation less the phases that were measured directly. Deriving it rather than
 * reimplementing it is deliberate: a copy of the merge in a benchmark would keep reporting the cost
 * of whichever version was pasted here, which is exactly how the merge came to be blamed for a cost
 * that was almost entirely somewhere else. Measured this way the roles were the reverse of the
 * expected one -- the merge loop was 27MiB of 629, and the change set's two relationship extractions
 * were 391.
 *
 * <p>Methodology is {@link MergeAllocationBenchmark}'s: thread-allocated bytes, one warm-up pass, and
 * the phases run in the constructor's order so each sees the heap state the real one would.
 */
public final class MergePhaseAllocationBenchmark {

    private static final double MEBIBYTE = 1024.0 * 1024.0;
    private static final ThreadMXBean THREADS =
            (ThreadMXBean) ManagementFactory.getThreadMXBean();

    private MergePhaseAllocationBenchmark() {
    }

    /**
     * Prints one line per phase.
     *
     * @param args Ignored.
     */
    public static void main(final String[] args) {
        THREADS.setThreadAllocatedMemoryEnabled(true);
        OOPSourceCodeModel base = SyntheticModels.baseRevision();
        OOPSourceCodeModel head = SyntheticModels.headRevision();

        phases(base, head, false);
        phases(base, head, true);
    }

    private static void phases(final OOPSourceCodeModel base, final OOPSourceCodeModel head,
                               final boolean report) {
        long mark = mark();
        CodeDiff diff = new CodeDiff(base, head);
        long whole = since(mark);
        if (diff.mergedModel().size() == 0) {
            throw new IllegalStateException("empty merge");
        }

        mark = mark();
        OOPSourceCodeModel headCopy = head.copy();
        long copy = since(mark);

        mark = mark();
        ChangeSet changeSet = new ChangeSet(base, head);
        long changeset = since(mark);
        if (changeSet.modifiedComponents() == null) {
            throw new IllegalStateException("no changeset");
        }

        mark = mark();
        new ExtractedRelationships(headCopy).result();
        long extraction = since(mark);

        if (!report) {
            return;
        }
        print("head.copy()", copy);
        print("new ChangeSet(base, head)", changeset);
        print("ExtractedRelationships(merged)", extraction);
        print("merge itself (derived)", whole - copy - changeset - extraction);
        System.out.printf(Locale.ROOT, "  %-34s %,15d bytes  %7.1f MiB%n",
                "WHOLE CodeDiff", whole, whole / MEBIBYTE);
    }

    private static long mark() {
        return THREADS.getCurrentThreadAllocatedBytes();
    }

    private static long since(final long mark) {
        return THREADS.getCurrentThreadAllocatedBytes() - mark;
    }

    private static void print(final String phase, final long allocated) {
        System.out.printf(Locale.ROOT, "  %-34s %,15d bytes  %7.1f MiB%n",
                phase, allocated, allocated / MEBIBYTE);
    }
}
