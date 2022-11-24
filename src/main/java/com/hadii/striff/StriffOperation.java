package com.hadii.striff;

import com.hadii.clarpse.compiler.ClarpseProject;
import com.hadii.clarpse.compiler.CompileException;
import com.hadii.clarpse.compiler.CompileResult;
import com.hadii.clarpse.compiler.ProjectFile;
import com.hadii.clarpse.compiler.ProjectFiles;
import com.hadii.striff.diagram.StriffCodeModel;
import com.hadii.striff.diagram.StriffDiagrams;
import com.hadii.striff.diagram.plantuml.PUMLDrawException;
import com.hadii.striff.parse.CodeDiff;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Entry point for Stiff diagram generation.
 */
public class StriffOperation {

    private static final Logger LOGGER = LogManager.getLogger(StriffOperation.class);
    private final StriffDiagrams diagrams;

    public StriffOperation(ProjectFiles originalFiles, ProjectFiles newFiles, StriffConfig config)
        throws NoStructuralChangesException, IOException, PUMLDrawException, CompileException {
        validateFilterFiles(originalFiles, newFiles, config.filesFilter);
        CompileResult originalCompileResult = new ClarpseProject(originalFiles).result();
        CompileResult newCompileResult = new ClarpseProject(newFiles).result();
        CodeDiff diffedModel = new CodeDiff(new StriffCodeModel(originalCompileResult.model()),
                                            new StriffCodeModel(newCompileResult.model()));
        Set<ProjectFile> combinedFailures = Stream.concat(
                                                      newCompileResult.failures().stream(),
                                                      originalCompileResult.failures().stream())
                                                  .collect(Collectors.toSet());
        this.diagrams = new StriffDiagrams(diffedModel, config, combinedFailures);
    }

    private void validateFilterFiles(ProjectFiles originalFiles, ProjectFiles newFiles,
                                     Set<String> filesFilter) {
        if (!filterFilesExistInProjects(originalFiles, newFiles, filesFilter)) {
            throw new IllegalArgumentException(
                "One or more filter file paths are invalid: " + filesFilter + ".");
        }
    }

    private boolean filterFilesExistInProjects(ProjectFiles originalFiles, ProjectFiles newFiles,
                                               Set<String> filesFilter) {
        return Stream.concat(originalFiles.files().stream(), newFiles.files().stream()).map(
            ProjectFile::path).collect(Collectors.toSet()).containsAll(filesFilter);
    }


    public StriffDiagrams result() {
        return this.diagrams;
    }
}
