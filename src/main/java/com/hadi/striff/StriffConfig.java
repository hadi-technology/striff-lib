package com.hadi.striff;

import com.hadi.clarpse.compiler.AnalysisOptions;
import com.hadi.clarpse.compiler.Lang;
import com.hadi.striff.diagram.display.DiagramColorScheme;
import com.hadi.striff.diagram.display.DiagramDisplayOverride;
import com.hadi.striff.diagram.display.LightDiagramColorScheme;
import com.hadi.striff.diagram.display.OutputMode;
import com.hadi.striff.diagram.plantuml.LayoutEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Config object used to inform the striff generation process with a fluent
 * interface design.
 */
public class StriffConfig {

    /** The default most context files a one-level analysis models per language. */
    public static final int DEFAULT_CONTEXT_BUDGET = 200;

    private static final Logger LOGGER = LoggerFactory.getLogger(StriffConfig.class);

    private OutputMode outputMode = OutputMode.DEFAULT;
    /**
     * Optional set of source files to restrict the analysis of architectural
     * differences to *specific* source files. When an empty set is provided,
     * striffs will display architectural differences encountered across all source
     * files.
     */
    private Set<String> filesFilter = Collections.emptySet();
    private Set<Lang> languages = new HashSet<>(Lang.supportedLanguages());
    // Control for SVG code generation
    private boolean metadataOnly = false;
    private DiagramColorScheme colorScheme = new LightDiagramColorScheme();
    private DiagramDisplayOverride displayOverride = null;
    private boolean enableAugmenters = true;
    /**
     * Source files whose components are forced into the diagram regardless of
     * whether they appear in {@link #filesFilter} or in keyRelationsComponents().
     * Components from these files that are not added/deleted/modified are
     * rendered as gray contextual. Callers use this to expand diagram scope
     * explicitly, typically with the files that use the changed code.
     *
     * <p>In a one-level analysis ({@link #analysisDepth()} of 1) these are the
     * <em>context files</em>: they are modelled as boundary components of both
     * revisions, without their own references being followed, and at most
     * {@link #contextBudget()} of them are modelled.
     */
    private Set<String> expandedFiles = Collections.emptySet();
    /**
     * Levels of referenced files modelled past {@link #filesFilter}: 0 for an
     * ordinary analysis, 1 for a one-level analysis.
     */
    private int analysisDepth = 0;
    private int levelOneBudget = AnalysisOptions.DEFAULT_LEVEL_ONE_BUDGET;
    private int contextBudget = DEFAULT_CONTEXT_BUDGET;
    private FocusExtender focusExtender = null;
    private LayoutEngine layoutEngine = LayoutEngine.SMETANA;
    /**
     * Hard limit to avoid sending extremely large diagrams to PlantUML.
     */
    private int maxComponentsPerDiagram = 120;

    public StriffConfig() {
    }

    public static StriffConfig create() {
        return new StriffConfig();
    }

    public StriffConfig setOutputMode(OutputMode outputMode) {
        this.outputMode = outputMode;
        return this;
    }

    public StriffConfig setFilesFilter(List<String> filesFilter) {
        this.filesFilter = filesFilter.stream()
                .filter(file -> Lang.supportedSourceFileExtns().stream().anyMatch(file::endsWith))
                .collect(Collectors.toSet());
        LOGGER.info("Setting list of filter files to: {}.", this.filesFilter);
        return this;
    }

    public StriffConfig setLanguages(Collection<Lang> languages) {
        this.languages = new HashSet<>(languages);
        return this;
    }


    public StriffConfig setMetadataOnly(boolean metadataOnly) {
        this.metadataOnly = metadataOnly;
        return this;
    }

    public StriffConfig setMaxComponentsPerDiagram(int maxComponentsPerDiagram) {
        if (maxComponentsPerDiagram <= 0) {
            throw new IllegalArgumentException("maxComponentsPerDiagram must be greater than zero.");
        }
        this.maxComponentsPerDiagram = maxComponentsPerDiagram;
        return this;
    }

    public StriffConfig setColorScheme(DiagramColorScheme colorScheme) {
        this.colorScheme = colorScheme;
        return this;
    }

    public StriffConfig setDisplayOverride(DiagramDisplayOverride displayOverride) {
        this.displayOverride = displayOverride;
        return this;
    }

    public StriffConfig setEnableAugmenters(boolean enableAugmenters) {
        this.enableAugmenters = enableAugmenters;
        return this;
    }

    public StriffConfig setLayoutEngine(LayoutEngine layoutEngine) {
        this.layoutEngine = layoutEngine;
        return this;
    }

    /**
     * Sets how many levels of referenced files are modelled past {@link #filesFilter()}.
     *
     * <p>0, the default, is an ordinary analysis. 1 is a one-level analysis: the files of the
     * filter are modelled in full, together with the repository files they reference, which are
     * modelled as boundary components; nothing further is compiled. It applies only when the
     * filter is non-empty, and only to the full pipeline.
     *
     * @param analysisDepth 0 or 1
     * @return this config
     * @throws IllegalArgumentException for any other depth
     */
    public StriffConfig setAnalysisDepth(int analysisDepth) {
        if (analysisDepth != 0 && analysisDepth != AnalysisOptions.MAX_DEPTH) {
            throw new IllegalArgumentException("analysisDepth must be 0 or " + AnalysisOptions.MAX_DEPTH
                    + ", got " + analysisDepth + ".");
        }
        this.analysisDepth = analysisDepth;
        return this;
    }

    /**
     * Sets the most level-one files a one-level analysis models per language.
     *
     * @param levelOneBudget the budget; must not be negative
     * @return this config
     */
    public StriffConfig setLevelOneBudget(int levelOneBudget) {
        if (levelOneBudget < 0) {
            throw new IllegalArgumentException("levelOneBudget must not be negative.");
        }
        this.levelOneBudget = levelOneBudget;
        return this;
    }

    /**
     * Sets the most context files ({@link #expandedFiles()}) a one-level analysis models per
     * language, separately from {@link #levelOneBudget()}.
     *
     * @param contextBudget the budget; must not be negative
     * @return this config
     */
    public StriffConfig setContextBudget(int contextBudget) {
        if (contextBudget < 0) {
            throw new IllegalArgumentException("contextBudget must not be negative.");
        }
        this.contextBudget = contextBudget;
        return this;
    }

    /**
     * Sets the hook a one-level analysis calls once, after its first compile, to add files to
     * the analysed set before the final compile. {@code null} removes it.
     *
     * @param focusExtender the hook, or {@code null}
     * @return this config
     */
    public StriffConfig setFocusExtender(FocusExtender focusExtender) {
        this.focusExtender = focusExtender;
        return this;
    }

    public StriffConfig setExpandedFiles(final Collection<String> files) {
        if (files == null) {
            this.expandedFiles = Collections.emptySet();
        } else {
            this.expandedFiles = new HashSet<>(files);
        }
        return this;
    }

    public OutputMode outputMode() {
        return this.outputMode;
    }

    public Set<String> filesFilter() {
        return this.filesFilter;
    }

    public DiagramColorScheme colorScheme() {
        return this.colorScheme;
    }

    public DiagramDisplayOverride displayOverride() {
        return this.displayOverride;
    }

    public boolean enableAugmenters() {
        return this.enableAugmenters;
    }

    /** Returns levels of referenced files modelled past the filter: 0 or 1. */
    public int analysisDepth() {
        return this.analysisDepth;
    }

    /** Returns the most level-one files a one-level analysis models per language. */
    public int levelOneBudget() {
        return this.levelOneBudget;
    }

    /** Returns the most context files a one-level analysis models per language. */
    public int contextBudget() {
        return this.contextBudget;
    }

    /** Returns the one-level focus extension hook, or {@code null} when there is none. */
    public FocusExtender focusExtender() {
        return this.focusExtender;
    }

    /**
     * Whether the full pipeline runs a one-level analysis: depth 1 with a non-empty filter.
     *
     * @return {@code true} for a one-level analysis
     */
    public boolean oneLevel() {
        return this.analysisDepth > 0 && !this.filesFilter.isEmpty();
    }

    /** Returns source files forced into the diagram as gray contextual components. */
    public Set<String> expandedFiles() {
        return this.expandedFiles;
    }

    public LayoutEngine layoutEngine() {
        return this.layoutEngine;
    }

    public Set<Lang> languages() {
        return this.languages;
    }


    public boolean metadataOnly() {
        return this.metadataOnly;
    }

    public int maxComponentsPerDiagram() {
        return this.maxComponentsPerDiagram;
    }

    @Override
    public String toString() {
        return "Output Mode: " + this.outputMode + ", Languages: " + this.languages + ", Filter Files: "
                + this.filesFilter + ", Max Components/Diagram: " + this.maxComponentsPerDiagram
                + ", Analysis Depth: " + this.analysisDepth;
    }
}
