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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Entry point for Stiff diagram generation.
 */
public class StriffOperation {

    private static final Logger LOGGER = LogManager.getLogger(StriffOperation.class);

    private final StriffOutput striffOutput;

    @LogExecutionTime
    public StriffOperation(ProjectFiles originalPFs, ProjectFiles newPFs, StriffConfig config)
            throws IOException, PUMLDrawException, CompileException {
        LOGGER.info("Starting new operation with config: " + config);
        validateProjectFiles(originalPFs, newPFs, config.filesFilter());
        filterConfigLanguages(config, originalPFs, newPFs);
        HashSet<CompileFailure> allFailures = new HashSet<>();
        LOGGER.info("Generating code diff metadata..");
        CodeDiff diffedModel = generateCodeDiff(originalPFs, newPFs, config, allFailures);
        LOGGER.info("Generating striff output metadata.. ");
        this.striffOutput = new StriffOutput(diffedModel, config, allFailures);
    }

    public StriffOperation(ProjectFiles originalPFs, ProjectFiles newPFs)
            throws PUMLDrawException, CompileException, IOException {
        this(originalPFs, newPFs, new StriffConfig());
    }

    @LogExecutionTime
    private static CodeDiff generateCodeDiff(ProjectFiles originalPFs, ProjectFiles newPFs,
            StriffConfig config,
            HashSet<CompileFailure> allFailures) throws CompileException {
        OOPSourceCodeModel oldModel = new OOPSourceCodeModel();
        OOPSourceCodeModel newModel = new OOPSourceCodeModel();
        for (Lang currLang : config.languages()) {
            CompileResult oldCR = new ClarpseProject(originalPFs, currLang).result();
            CompileResult newCR = new ClarpseProject(newPFs, currLang).result();
            allFailures.addAll(Stream.concat(newCR.failures().stream(),
                    oldCR.failures().stream()).collect(Collectors.toSet()));
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
        if (!filesFilter.isEmpty()) {
            LOGGER.info("Filter files list is not empty, filtering down project files..");
            originalFiles.filter(filesFilter);
            newFiles.filter(filesFilter);
        }
    }

    /**
     * Filters the config languages to only include languages that have at least one file
     * in either the original or new ProjectFiles. This optimization avoids attempting
     * compilation for languages with no matching files.
     *
     * @param config The config to modify
     * @param originalFiles The original project files
     * @param newFiles The new project files
     */
    private static void filterConfigLanguages(StriffConfig config, ProjectFiles originalFiles, ProjectFiles newFiles) {
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
            LOGGER.info("Skipping compilation for languages with no matching files: " + skippedLanguages);
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

    public StriffOutput result() {
        return this.striffOutput;
    }
}
