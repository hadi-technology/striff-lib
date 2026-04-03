package com.hadi.striff;

import com.hadi.clarpse.compiler.ClarpseProject;
import com.hadi.clarpse.compiler.CompileException;
import com.hadi.clarpse.compiler.CompileFailure;
import com.hadi.clarpse.compiler.CompileResult;
import com.hadi.clarpse.compiler.Lang;
import com.hadi.clarpse.compiler.ProjectFile;
import com.hadi.clarpse.compiler.ProjectFiles;
import com.hadi.clarpse.sourcemodel.OOPSourceCodeModel;
import com.hadi.striff.annotations.LogExecutionTime;
import com.hadi.striff.diagram.StriffOutput;
import com.hadi.striff.diagram.plantuml.PUMLDrawException;
import com.hadi.striff.parse.CodeDiff;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Entry point for Striff diagram generation.
 *
 * <p>Two construction modes are supported:</p>
 * <ul>
 *   <li><strong>Full pipeline</strong> — parses source files into OOP models, computes a
 *       {@link CodeDiff}, runs SPI augmenters, and renders SVG diagrams.</li>
 *   <li><strong>Render-only</strong> — accepts a pre-built {@link CodeDiff} and skips
 *       parsing entirely. This allows a second render pass (e.g. with AI augmentation
 *       enabled on a background thread) without re-parsing the source files.</li>
 * </ul>
 */
public class StriffOperation {

    private static final Logger LOGGER = LoggerFactory.getLogger(StriffOperation.class);

    private final CodeDiff codeDiff;
    private final StriffOutput striffOutput;
    private final Set<CompileFailure> compileFailures;

    /**
     * Full pipeline: parse source files, compute a CodeDiff, and render diagrams.
     *
     * <p>After construction, call {@link #codeDiff()} and {@link #compileFailures()}
     * to obtain the intermediate state needed for a subsequent render-only pass.</p>
     *
     * @param originalPFs original project files
     * @param newPFs      updated project files
     * @param config      generation configuration
     * @throws IOException       if rendered diagram output cannot be produced
     * @throws PUMLDrawException if PlantUML cannot render the requested diagram
     * @throws CompileException  if source parsing fails
     */
    @LogExecutionTime
    public StriffOperation(ProjectFiles originalPFs, ProjectFiles newPFs, StriffConfig config)
            throws IOException, PUMLDrawException, CompileException {
        LOGGER.info("Starting new operation with config: {}", config);
        validateProjectFiles(originalPFs, newPFs, config.filesFilter());
        filterConfigLanguages(config, originalPFs, newPFs);
        this.compileFailures = new HashSet<>();
        LOGGER.info("Generating code diff metadata..");
        this.codeDiff = generateCodeDiff(originalPFs, newPFs, config, this.compileFailures);
        LOGGER.info("Generating striff output metadata..");
        this.striffOutput = new StriffOutput(this.codeDiff, config, this.compileFailures);
    }

    /**
     * Render-only: produce diagrams from an already-parsed {@link CodeDiff}.
     *
     * <p>This skips source parsing and language detection entirely. The caller is
     * responsible for providing a valid CodeDiff (typically obtained from a prior
     * full-pipeline construction via {@link #codeDiff()}).</p>
     *
     * <p>Primary use case: re-rendering the same parsed models with a different
     * {@link StriffConfig} — for example, enabling AI augmentation on a background
     * thread while the base diagram has already been returned to the client.</p>
     *
     * @param codeDiff        pre-built code diff from a prior full-pipeline run
     * @param config          generation configuration (may differ from the original)
     * @param compileFailures compile failures from the original parse pass
     * @throws IOException       if rendered diagram output cannot be produced
     * @throws PUMLDrawException if PlantUML cannot render the requested diagram
     */
    @LogExecutionTime
    public StriffOperation(CodeDiff codeDiff, StriffConfig config,
                           Set<CompileFailure> compileFailures)
            throws IOException, PUMLDrawException {
        LOGGER.info("Starting render-only operation from existing CodeDiff with config: {}", config);
        this.codeDiff = Objects.requireNonNull(codeDiff, "codeDiff must not be null");
        if (compileFailures == null) {
            this.compileFailures = new HashSet<>();
        } else {
            this.compileFailures = compileFailures;
        }
        LOGGER.info("Generating striff output metadata..");
        this.striffOutput = new StriffOutput(codeDiff, config, this.compileFailures);
    }

    /**
     * Full pipeline with default configuration.
     */
    public StriffOperation(ProjectFiles originalPFs, ProjectFiles newPFs)
            throws PUMLDrawException, CompileException, IOException {
        this(originalPFs, newPFs, new StriffConfig());
    }

    /**
     * Returns the parsed {@link CodeDiff} produced during the full-pipeline construction.
     *
     * <p>This is the intermediate representation between parsing and rendering. Callers
     * can pass it to the render-only constructor to produce a second set of diagrams
     * (e.g. with AI enrichment) without re-parsing source files.</p>
     *
     * @return the code diff, never null after construction
     */
    public CodeDiff codeDiff() {
        return this.codeDiff;
    }

    /**
     * Returns the compile failures encountered during source parsing.
     *
     * <p>Needed by the render-only constructor to maintain consistent warning reporting
     * across both render passes.</p>
     *
     * @return compile failures set, never null
     */
    public Set<CompileFailure> compileFailures() {
        return this.compileFailures;
    }

    /**
     * Returns the generated Striff output for this operation.
     *
     * @return diagram output and compile warnings
     */
    public StriffOutput result() {
        return this.striffOutput;
    }

    @LogExecutionTime
    private static CodeDiff generateCodeDiff(ProjectFiles originalPFs, ProjectFiles newPFs,
            StriffConfig config,
            Set<CompileFailure> allFailures) throws CompileException {
        OOPSourceCodeModel oldModel = new OOPSourceCodeModel();
        OOPSourceCodeModel newModel = new OOPSourceCodeModel();
        Set<String> filesFilter = config.filesFilter();
        // Pass null instead of empty set to analyze all files
        Collection<String> pathsToAnalyze;
        String pathsToAnalyzeStr;
        if (filesFilter.isEmpty()) {
            pathsToAnalyze = null;
            pathsToAnalyzeStr = "null (all files)";
        } else {
            pathsToAnalyze = filesFilter;
            pathsToAnalyzeStr = String.valueOf(filesFilter);
        }
        LOGGER.info("pathsToAnalyze: {}, filesFilter size: {}", pathsToAnalyzeStr, filesFilter.size());
        for (Lang currLang : config.languages()) {
            LOGGER.info("Processing language: {}", currLang);
            CompileResult oldCR = new ClarpseProject(originalPFs, currLang, pathsToAnalyze).result();
            CompileResult newCR = new ClarpseProject(newPFs, currLang, pathsToAnalyze).result();

            long oldComponentCount = oldCR.model().components().count();
            long newComponentCount = newCR.model().components().count();
            LOGGER.info("Old model components: {}, New model components: {}", oldComponentCount, newComponentCount);

            allFailures.addAll(Stream.concat(newCR.failures().stream(),
                    oldCR.failures().stream()).collect(Collectors.toSet()));
            if (!allFailures.isEmpty()) {
                LOGGER.info("Compile failures for {}: {}", currLang, allFailures.size());
            }
            oldModel.merge(oldCR.model());
            newModel.merge(newCR.model());
        }
        LOGGER.info("Generating code diff b/w old and new code models..");
        return new CodeDiff(oldModel, newModel);
    }

    private void validateProjectFiles(ProjectFiles originalFiles, ProjectFiles newFiles,
            Set<String> filesFilter) {
        LOGGER.info("Validating input project files..");
        if (!filterFilesExistInProjects(originalFiles, newFiles, filesFilter)) {
            throw new IllegalArgumentException("One or more filter file paths are invalid: " + filesFilter + ".");
        }
    }

    /**
     * Filters the config languages to only include languages that have at least one file
     * in either the original or new ProjectFiles. This optimization avoids attempting
     * compilation for languages with no matching files.
     *
     * @param config        The config to modify
     * @param originalFiles The original project files
     * @param newFiles      The new project files
     */
    private static void filterConfigLanguages(StriffConfig config, ProjectFiles originalFiles,
            ProjectFiles newFiles) {
        // Detect which languages have files in the ProjectFiles
        Set<Lang> languagesInOriginal = detectLanguagesInProjectFiles(originalFiles);
        Set<Lang> languagesInNew = detectLanguagesInProjectFiles(newFiles);
        Set<Lang> languagesWithFiles = new HashSet<>(languagesInOriginal);
        languagesWithFiles.addAll(languagesInNew);

        LOGGER.info("Detected languages in original files: {}", languagesInOriginal);
        LOGGER.info("Detected languages in new files: {}", languagesInNew);
        LOGGER.info("Configured languages before filtering: {}", config.languages());

        // Filter config languages to only include those with actual files
        Set<Lang> filteredLanguages = config.languages().stream()
                .filter(languagesWithFiles::contains)
                .collect(Collectors.toSet());

        if (filteredLanguages.isEmpty()) {
            LOGGER.warn("No languages have matching files in the filtered project. Configured languages: "
                    + config.languages() + ", Languages with files: " + languagesWithFiles);
        } else if (filteredLanguages.size() < config.languages().size()) {
            Set<Lang> skippedLanguages = new HashSet<>(config.languages());
            skippedLanguages.removeAll(filteredLanguages);
            LOGGER.info("Skipping compilation for languages with no matching files: {}", skippedLanguages);
            config.setLanguages(filteredLanguages);
            LOGGER.info("Configured languages after filtering: {}", config.languages());
        } else {
            LOGGER.info("All configured languages have files, no filtering needed.");
        }
    }

    /**
     * Detects which languages have at least one file in the given ProjectFiles.
     * This is done by checking file extensions against known language extensions.
     *
     * @param projectFiles The project files to analyze
     * @return Set of languages that have at least one file
     */
    private static Set<Lang> detectLanguagesInProjectFiles(ProjectFiles projectFiles) {
        Set<Lang> languagesFound = new HashSet<>();
        for (ProjectFile file : projectFiles.files()) {
            Lang lang = Lang.langFromExtn(file.extension());
            if (lang != null) {
                languagesFound.add(lang);
            }
        }
        return languagesFound;
    }

    private boolean filterFilesExistInProjects(ProjectFiles originalPFs, ProjectFiles newPFs,
            Set<String> filesFilter) {
        return Stream.concat(originalPFs.files().stream(), newPFs.files().stream())
                .map(ProjectFile::path).collect(Collectors.toSet()).containsAll(filesFilter);
    }
}
