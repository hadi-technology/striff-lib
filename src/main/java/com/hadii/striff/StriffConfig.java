package com.hadii.striff;

import com.hadii.clarpse.compiler.Lang;
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
 * Config object used to inform the striff generation process.
 */
public class StriffConfig {

    private static final Logger LOGGER = LogManager.getLogger(StriffConfig.class);

    public OutputMode outputMode = OutputMode.DEFAULT;
    /**
     * Optional set of source files to restrict the analysis of architectural differences
     * to *specific* source files. When an empty set is provided, striffs will display
     * architectural differences encountered across all source files.
     */
    public Set<String> filesFilter = Collections.emptySet();
    private Set<Lang> languages = new HashSet<>(Lang.supportedLanguages());


    public StriffConfig() { }

    public StriffConfig(OutputMode outputMode, Collection<Lang> languages) {
        this.outputMode = outputMode;
        this.languages = new HashSet<>(languages);
    }

    public StriffConfig(OutputMode outputMode) {
        this.outputMode = outputMode;
    }

    /**
     * Constructs a StriffConfig object.
     *
     * @param outputMode  Desired output mode for striff diagrams.
     * @param filesFilter A set of files to restrict the analysis of architectural differences to.
     */
    public StriffConfig(OutputMode outputMode, List<String> filesFilter) {
        this.outputMode = outputMode;
        this.filesFilter =
            filesFilter.stream().filter(file -> Lang.supportedSourceFileExtns().stream().anyMatch(file::endsWith)).collect(Collectors.toSet());
        LOGGER.info("Setting list of filter files to: " + this.filesFilter + ".");
    }

    /**
     * Constructs a StriffConfig object.
     *
     * @param outputMode  Desired output mode for striff diagrams.
     * @param languages   Desired programming languages to use for the analysis.
     * @param filesFilter A set of files to restrict the analysis of architectural differences to.
     */
    public StriffConfig(OutputMode outputMode, Collection<Lang> languages,
                        List<String> filesFilter) {
        this(outputMode, languages);
        this.languages = new HashSet<>(Lang.supportedLanguages());
        this.filesFilter =
            filesFilter.stream().filter(file -> Lang.supportedSourceFileExtns().stream().anyMatch(file::endsWith)).collect(Collectors.toSet());
        LOGGER.info("Setting list of filter files to: " + this.filesFilter + ".");
    }

    @Override
    public String toString() {
        return "Output Mode: " + this.outputMode + ", Languages: " + this.languages + ", Filter "
            + "Files: " + this.filesFilter;
    }

    public Set<Lang> languages() {
        return this.languages;
    }
}
