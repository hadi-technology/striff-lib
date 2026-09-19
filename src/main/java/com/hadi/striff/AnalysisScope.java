package com.hadi.striff;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

/**
 * What a one-level analysis modelled beyond the filter's files, and what its budgets held back.
 *
 * <p>Every set holds paths in the form of {@code ProjectFile.path()}, sorted, across all languages
 * and both revisions. An ordinary analysis has none of these, and is described by {@link #none()}.
 */
public final class AnalysisScope {

    private static final AnalysisScope NONE =
            new AnalysisScope(Set.of(), Set.of(), Set.of(), Set.of(), Set.of());

    private final Set<String> levelOneFiles;
    private final Set<String> levelOneHeldByBudget;
    private final Set<String> contextFiles;
    private final Set<String> contextHeldByBudget;
    private final Set<String> extendedFocus;

    /**
     * Creates a scope.
     *
     * @param levelOneFiles        files modelled as boundary because analysed files reference them
     * @param levelOneHeldByBudget referenced files the level-one budget kept out of the model
     * @param contextFiles         context files modelled as boundary
     * @param contextHeldByBudget  context files the context budget kept out of the model
     * @param extendedFocus        files the focus extender added to the analysed files
     */
    public AnalysisScope(Collection<String> levelOneFiles, Collection<String> levelOneHeldByBudget,
                         Collection<String> contextFiles, Collection<String> contextHeldByBudget,
                         Collection<String> extendedFocus) {
        this.levelOneFiles = sorted(levelOneFiles);
        this.levelOneHeldByBudget = sorted(levelOneHeldByBudget);
        this.contextFiles = sorted(contextFiles);
        this.contextHeldByBudget = sorted(contextHeldByBudget);
        this.extendedFocus = sorted(extendedFocus);
    }

    /**
     * The scope of an ordinary analysis: nothing beyond the analysed files.
     *
     * @return the empty scope
     */
    public static AnalysisScope none() {
        return NONE;
    }

    private static Set<String> sorted(Collection<String> paths) {
        if (paths == null) {
            return Set.of();
        }
        return Collections.unmodifiableSet(new TreeSet<>(paths));
    }

    /** Returns the files modelled as boundary because analysed files reference them. */
    public Set<String> levelOneFiles() {
        return levelOneFiles;
    }

    /** Returns the referenced files the level-one budget kept out of the model. */
    public Set<String> levelOneHeldByBudget() {
        return levelOneHeldByBudget;
    }

    /** Returns the context files modelled as boundary. */
    public Set<String> contextFiles() {
        return contextFiles;
    }

    /** Returns the context files the context budget kept out of the model. */
    public Set<String> contextHeldByBudget() {
        return contextHeldByBudget;
    }

    /** Returns the files the focus extender added to the analysed files. */
    public Set<String> extendedFocus() {
        return extendedFocus;
    }

    @Override
    public String toString() {
        return "levelOne=" + levelOneFiles.size() + ", levelOneHeld=" + levelOneHeldByBudget.size()
                + ", context=" + contextFiles.size() + ", contextHeld=" + contextHeldByBudget.size()
                + ", extendedFocus=" + extendedFocus.size();
    }
}
