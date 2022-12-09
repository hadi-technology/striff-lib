package com.hadii.striff;

import java.io.IOException;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.hadii.clarpse.compiler.ClarpseProject;
import com.hadii.clarpse.compiler.CompileException;
import com.hadii.clarpse.compiler.CompileResult;
import com.hadii.clarpse.compiler.ProjectFile;
import com.hadii.clarpse.compiler.ProjectFiles;
import com.hadii.striff.diagram.StriffCodeModel;
import com.hadii.striff.diagram.StriffDiagrams;
import com.hadii.striff.diagram.plantuml.PUMLDrawException;
import com.hadii.striff.parse.CodeDiff;

/**
 * Entry point for Stiff diagram generation.
 */
public class StriffOperation {

    private final StriffDiagrams diagrams;

    public StriffOperation(ProjectFiles originalPFs, ProjectFiles newPFs, StriffConfig config)
        throws NoStructuralChangesException, IOException, PUMLDrawException, CompileException {
        validateFilterFiles(originalPFs, newPFs, config.filesFilter);
        CompileResult originalCompileResult = new ClarpseProject(originalPFs.files(),
                                                                 originalPFs.lang()).result();
        CompileResult newCompileResult = new ClarpseProject(newPFs.files(), newPFs.lang()).result();
        CodeDiff diffedModel = new CodeDiff(new StriffCodeModel(originalCompileResult.model()),
                                            new StriffCodeModel(newCompileResult.model()));
        Set<ProjectFile> combinedFailures = Stream.concat(
                                                      newCompileResult.failures().stream(),
                                                      originalCompileResult.failures().stream())
                                                  .collect(Collectors.toSet());
        this.diagrams = new StriffDiagrams(diffedModel, config, combinedFailures);
    }

    public StriffOperation(ProjectFiles originalPFs, ProjectFiles newPFs) throws PUMLDrawException,
        NoStructuralChangesException, CompileException, IOException {
        this(originalPFs, newPFs, new StriffConfig());
    }

    private void validateFilterFiles(ProjectFiles originalFiles, ProjectFiles newFiles,
                                     Set<String> filesFilter) {
        if (!filterFilesExistInProjects(originalFiles, newFiles, filesFilter)) {
            throw new IllegalArgumentException(
                "One or more filter file paths are invalid: " + filesFilter + ".");
        }
    }

    private boolean filterFilesExistInProjects(ProjectFiles originalPFs, ProjectFiles newPFs,
                                               Set<String> filesFilter) {
        return Stream.concat(originalPFs.files().stream(), newPFs.files().stream()).map(
            ProjectFile::path).collect(Collectors.toSet()).containsAll(filesFilter);
    }


    public StriffDiagrams result() {
        return this.diagrams;
    }
}
