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
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Entry point for Striff diagram generation.
 *
 * <p>Three construction modes are supported:</p>
 * <ul>
 *   <li><strong>Full pipeline</strong> — parses source files into OOP models, computes a
 *       {@link CodeDiff}, runs SPI augmenters, and renders SVG diagrams.</li>
 *   <li><strong>Incremental</strong> — accepts a cached base model and parses only
 *       changed files, then merges and compares to produce the diff.</li>
 *   <li><strong>Render-only</strong> — accepts a pre-built {@link CodeDiff} and skips
 *       parsing entirely. This allows a second render pass with a different
 *       configuration without re-parsing the source files.</li>
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
     * {@link StriffConfig} — for example, with a different layout engine or
     * zoom level on a background thread while the base diagram has already been
     * returned to the client.</p>
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
     * Incremental parsing: reuses a cached base model and parses only changed files.
     *
     * <p>This constructor is optimized for scenarios where you have a previously parsed
     * base model and only a small subset of files have changed. It parses only the
     * specified changed files from the new ProjectFiles, merges them with a copy of
     * the base model, and computes the diff.</p>
     *
     * <p><strong>Use case:</strong> CI/CD pipelines processing PRs with small changes.
     * For a 1000-file codebase with 5 changed files, this parses only 5 files instead
     * of 2000 (1000 old + 1000 new).</p>
     *
     * <p>The cached base model can be obtained from a prior full-pipeline run via
     * {@link CodeDiff#newModel()}.</p>
     *
     * @param baseModel    cached OOP model from a previous run (typically from
     *                     {@code priorOp.codeDiff().newModel()})
     * @param newFiles     updated project files containing the changed files
     * @param changedFiles set of file paths that have changed (relative to project root)
     * @param config       generation configuration
     * @throws IOException       if rendered diagram output cannot be produced
     * @throws PUMLDrawException if PlantUML cannot render the requested diagram
     * @throws CompileException  if source parsing fails
     */
    @LogExecutionTime
    public StriffOperation(OOPSourceCodeModel baseModel, ProjectFiles newFiles,
                           Set<String> changedFiles, StriffConfig config)
            throws IOException, PUMLDrawException, CompileException {
        if (baseModel == null) {
            throw new IllegalArgumentException("baseModel must not be null");
        }
        if (changedFiles == null || changedFiles.isEmpty()) {
            throw new IllegalArgumentException("changedFiles must not be null or empty");
        }
        LOGGER.info("Starting incremental operation with {} changed files", changedFiles.size());
        filterConfigLanguages(config, newFiles, newFiles);
        this.compileFailures = new HashSet<>();
        LOGGER.info("Generating code diff metadata (incremental)...");
        this.codeDiff = generateCodeDiffIncremental(baseModel, newFiles, changedFiles, config, this.compileFailures);
        LOGGER.info("Generating striff output metadata..");
        this.striffOutput = new StriffOutput(this.codeDiff, config, this.compileFailures);
    }

    /**
     * Returns the parsed {@link CodeDiff} produced during the full-pipeline construction.
     *
     * <p>This is the intermediate representation between parsing and rendering. Callers
     * can pass it to the render-only constructor to produce additional diagrams
     * (e.g. with different styling or layout) without re-parsing source files.</p>
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
            // Compile old and new in parallel for better performance
            var oldCRFuture = java.util.concurrent.CompletableFuture.supplyAsync(() -> {
                try {
                    return new ClarpseProject(originalPFs, currLang, pathsToAnalyze).result();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
            var newCRFuture = java.util.concurrent.CompletableFuture.supplyAsync(() -> {
                try {
                    return new ClarpseProject(newPFs, currLang, pathsToAnalyze).result();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });

            CompileResult oldCR = joinCompileResult(oldCRFuture);
            CompileResult newCR = joinCompileResult(newCRFuture);

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
        if (config.resolveContextualComponents() && !filesFilter.isEmpty()) {
            resolveMissingContextualComponents(oldModel, newModel, originalPFs, newPFs,
                    config.languages(), allFailures);
        }
        LOGGER.info("Generating code diff b/w old and new code models..");
        return new CodeDiff(oldModel, newModel);
    }

    private static void resolveMissingContextualComponents(
            OOPSourceCodeModel oldModel, OOPSourceCodeModel newModel,
            ProjectFiles originalPFs, ProjectFiles newPFs,
            Set<Lang> languages, Set<CompileFailure> allFailures) {
        Set<String> missingNames = collectUnresolvedReferences(oldModel, newModel);
        if (missingNames.isEmpty()) {
            return;
        }
        LOGGER.info("Found {} potentially missing component references for contextual resolution", missingNames.size());
        for (Lang lang : languages) {
            Set<String> filesToParse = new HashSet<>();
            for (String missingName : missingNames) {
                findSourceFile(missingName, lang, originalPFs, newPFs)
                        .ifPresent(filesToParse::add);
            }
            if (filesToParse.isEmpty()) {
                continue;
            }
            LOGGER.info("Parsing {} additional source files for {} to resolve contextual components",
                    filesToParse.size(), lang);
            try {
                CompileResult oldCR = new ClarpseProject(originalPFs, lang, filesToParse).result();
                CompileResult newCR = new ClarpseProject(newPFs, lang, filesToParse).result();
                allFailures.addAll(oldCR.failures());
                allFailures.addAll(newCR.failures());
                oldModel.merge(oldCR.model());
                newModel.merge(newCR.model());
            } catch (Exception e) {
                LOGGER.warn("Failed to parse additional contextual source files: {}", e.getMessage());
            }
        }
    }

    private static Set<String> collectUnresolvedReferences(
            OOPSourceCodeModel oldModel, OOPSourceCodeModel newModel) {
        Set<String> missing = new HashSet<>();
        Stream.concat(oldModel.components(), newModel.components()).forEach(cmp -> {
            Stream.concat(cmp.internalDependencies().stream(), cmp.externalDependencies().stream())
                    .forEach(ref -> {
                        String target = ref.invokedComponent();
                        if (!oldModel.containsComponent(target) && !newModel.containsComponent(target)) {
                            missing.add(target);
                        }
                    });
        });
        return missing;
    }

    private static Optional<String> findSourceFile(String componentUniqueName, Lang lang,
            ProjectFiles originalPFs, ProjectFiles newPFs) {
        String simpleName = extractTopLevelClassName(componentUniqueName);
        String fileName = simpleName + "." + lang.sourceFileExtns().iterator().next();
        Set<ProjectFile> matches = new HashSet<>();
        matches.addAll(originalPFs.matchingFilesByName(fileName));
        matches.addAll(newPFs.matchingFilesByName(fileName));
        if (matches.isEmpty()) {
            return Optional.empty();
        }
        if (matches.size() == 1) {
            return Optional.of(matches.iterator().next().path());
        }
        String packagePath = extractPackagePath(componentUniqueName);
        if (!packagePath.isEmpty()) {
            Optional<String> best = matches.stream()
                    .filter(pf -> pf.path().contains(packagePath))
                    .findFirst().map(ProjectFile::path);
            if (best.isPresent()) {
                return best;
            }
        }
        return Optional.of(matches.iterator().next().path());
    }

    private static String extractTopLevelClassName(String uniqueName) {
        String name = uniqueName;
        if (name.contains("$")) {
            name = name.substring(0, name.indexOf("$"));
        }
        if (name.contains(".")) {
            name = name.substring(name.lastIndexOf(".") + 1);
        }
        return name;
    }

    private static String extractPackagePath(String uniqueName) {
        int lastDot = uniqueName.lastIndexOf(".");
        if (lastDot <= 0) {
            return "";
        }
        String pkg = uniqueName.substring(0, lastDot);
        if (pkg.contains("$")) {
            pkg = pkg.substring(0, pkg.indexOf("$"));
        }
        return pkg.replace(".", "/");
    }

    /**
     *
     * <p>This method reuses the cached base model and parses only the changed files
     * from the new ProjectFiles. The changed components are merged with a copy of
     * the base model to create the "new" state for comparison.</p>
     *
     * @param baseModel      cached OOP model from a previous run
     * @param newFiles       updated project files
     * @param changedFiles   set of file paths that have changed
     * @param config         generation configuration
     * @param allFailures    collection to accumulate compile failures
     * @return CodeDiff comparing base model vs updated model
     * @throws CompileException if parsing fails
     */
    @LogExecutionTime
    private static CodeDiff generateCodeDiffIncremental(OOPSourceCodeModel baseModel,
                                                         ProjectFiles newFiles,
                                                         Set<String> changedFiles,
                                                         StriffConfig config,
                                                         Set<CompileFailure> allFailures) throws CompileException {
        // The base model serves as the "old" state
        OOPSourceCodeModel oldModel = baseModel;
        // Create a copy of base model and merge changed components to create "new" state
        OOPSourceCodeModel newModel = baseModel.copy();
        long baseComponentCount = baseModel.components().count();
        LOGGER.info("Base model components: {}", baseComponentCount);

        // Parse only the changed files
        for (Lang currLang : config.languages()) {
            // Filter changed files to only those matching current language
            Set<String> languageChangedFiles = filterFilesByLanguage(newFiles, changedFiles, currLang);
            if (languageChangedFiles.isEmpty()) {
                LOGGER.info("No changed files for language: {}", currLang);
                continue;
            }
            LOGGER.info("Processing {} changed files for language: {}", languageChangedFiles.size(), currLang);
            CompileResult newCR = new ClarpseProject(newFiles, currLang, languageChangedFiles).result();
            long changedComponentCount = newCR.model().components().count();
            LOGGER.info("Parsed {} components from {} changed {} files",
                    changedComponentCount, languageChangedFiles.size(), currLang);

            allFailures.addAll(newCR.failures());
            if (!allFailures.isEmpty()) {
                LOGGER.info("Compile failures for {}: {}", currLang, newCR.failures().size());
            }
            // Merge changed components into the copied base model
            newModel.merge(newCR.model());
        }
        long newComponentCount = newModel.components().count();
        LOGGER.info("New model components: {} (base: {}, delta: {})",
                newComponentCount, baseComponentCount, newComponentCount - baseComponentCount);
        LOGGER.info("Generating code diff b/w base and updated code models..");
        return new CodeDiff(oldModel, newModel);
    }

    private static CompileResult joinCompileResult(java.util.concurrent.CompletableFuture<CompileResult> future) throws CompileException {
        try {
            return future.join();
        } catch (java.util.concurrent.CompletionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof CompileException ce) {
                throw ce;
            }
            if (cause instanceof RuntimeException re) {
                throw re;
            }
            throw new RuntimeException(cause);
        }
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

    /**
     * Filters a set of file paths to only include files matching the given language.
     *
     * @param projectFiles the project files to check extensions against
     * @param filePaths    the file paths to filter
     * @param lang         the language to filter by
     * @return set of file paths that match the language
     */
    private static Set<String> filterFilesByLanguage(ProjectFiles projectFiles,
                                                      Set<String> filePaths,
                                                      Lang lang) {
        Set<String> filtered = new HashSet<>();
        // Get files for this language and check if they're in the changed set
        for (ProjectFile file : projectFiles.files(lang)) {
            // Normalize path for comparison (handle both / and \ separators)
            String normalizedPath = file.path().replace("\\", "/");
            for (String changedPath : filePaths) {
                String normalizedChangedPath = changedPath.replace("\\", "/");
                // Check if paths match (handling cases with/without leading /)
                if (normalizedPath.equals(normalizedChangedPath)
                        || normalizedPath.endsWith("/" + normalizedChangedPath)
                        || normalizedChangedPath.endsWith("/" + normalizedPath)) {
                    filtered.add(file.path());
                    break;
                }
            }
        }
        return filtered;
    }

    private boolean filterFilesExistInProjects(ProjectFiles originalPFs, ProjectFiles newPFs,
            Set<String> filesFilter) {
        return Stream.concat(originalPFs.files().stream(), newPFs.files().stream())
                .map(ProjectFile::path).collect(Collectors.toSet()).containsAll(filesFilter);
    }
}
