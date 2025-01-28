package com.hadii.striff;

import com.hadii.clarpse.compiler.Lang;
import com.hadii.striff.diagram.display.DiagramColorScheme;
import com.hadii.striff.diagram.display.LightDiagramColorScheme;
import com.hadii.striff.diagram.display.OutputMode;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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

    private static final Logger LOGGER = LogManager.getLogger(StriffConfig.class);

    private OutputMode outputMode = OutputMode.DEFAULT;
    /**
     * Optional set of source files to restrict the analysis of architectural
     * differences to *specific* source files. When an empty set is provided,
     * striffs will display architectural differences encountered across all source
     * files.
     */
    private Set<String> filesFilter = Collections.emptySet();
    private Set<Lang> languages = new HashSet<>(Lang.supportedLanguages());
    private boolean processMetrics = true;
    // Control for SVG code generation
    private boolean metadataOnly = false;
    private DiagramColorScheme colorScheme = new LightDiagramColorScheme();

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
        LOGGER.info("Setting list of filter files to: " + this.filesFilter + ".");
        return this;
    }

    public StriffConfig setLanguages(Collection<Lang> languages) {
        this.languages = new HashSet<>(languages);
        return this;
    }

    public StriffConfig setProcessMetrics(boolean processMetrics) {
        this.processMetrics = processMetrics;
        return this;
    }

    public StriffConfig setMetadataOnly(boolean metadataOnly) {
        this.metadataOnly = metadataOnly;
        return this;
    }

    public StriffConfig setColorScheme(DiagramColorScheme colorScheme) {
        this.colorScheme = colorScheme;
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

    public Set<Lang> languages() {
        return this.languages;
    }

    public boolean processMetrics() {
        return this.processMetrics;
    }

    public boolean metadataOnly() {
        return this.metadataOnly;
    }

    @Override
    public String toString() {
        return "Output Mode: " + this.outputMode + ", Languages: " + this.languages + ", Filter Files: "
                + this.filesFilter;
    }
}
