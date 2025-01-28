package com.hadii.striff;

import com.hadii.clarpse.compiler.ClarpseProject;
import com.hadii.clarpse.compiler.CompileException;
import com.hadii.clarpse.compiler.CompileResult;
import com.hadii.clarpse.compiler.Lang;
import com.hadii.clarpse.compiler.ProjectFile;
import com.hadii.clarpse.compiler.ProjectFiles;
import com.hadii.clarpse.sourcemodel.OOPSourceCodeModel;
import com.hadii.striff.annotations.LogExecutionTime;
import com.hadii.striff.diagram.StriffOutput;
import com.hadii.striff.diagram.plantuml.PUMLDrawException;
import com.hadii.striff.parse.CodeDiff;
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
        HashSet<ProjectFile> allFailures = new HashSet<>();
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
            HashSet<ProjectFile> allFailures) throws CompileException {
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

    private boolean filterFilesExistInProjects(ProjectFiles originalPFs, ProjectFiles newPFs,
            Set<String> filesFilter) {
        return Stream.concat(originalPFs.files().stream(), newPFs.files().stream())
                .map(ProjectFile::path).collect(Collectors.toSet()).containsAll(filesFilter);
    }

    public StriffOutput result() {
        return this.striffOutput;
    }
}
