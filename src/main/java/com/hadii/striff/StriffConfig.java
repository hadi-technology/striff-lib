package com.hadii.striff;

import com.hadii.clarpse.compiler.Lang;
import com.hadii.striff.diagram.display.OutputMode;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collections;
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

    public StriffConfig(OutputMode outputMode) {
        this.outputMode = outputMode;
    }

    /**
     * Constructs a StriffConfig object.
     *
     * @param filesFilter A set of files to restrict the analysis of architectural differences to.
     * @param outputMode  Desired output mode for striff diagrams.
     */
    public StriffConfig(List<String> filesFilter,
                        OutputMode outputMode) {
        this(outputMode);
        this.filesFilter = filesFilter.stream().filter(
            file -> Lang.supportedSourceFileExtns().stream().anyMatch(
                file::endsWith)).collect(Collectors.toSet());
        LOGGER.info("Setting list of filter files to: " + this.filesFilter + ".");
    }

    public StriffConfig(List<String> filesFilter) {
        this(filesFilter, OutputMode.DEFAULT);
    }

    public StriffConfig(int softMaxSizeLimit) {
        this(Collections.emptyList(), OutputMode.DEFAULT);
    }
}
