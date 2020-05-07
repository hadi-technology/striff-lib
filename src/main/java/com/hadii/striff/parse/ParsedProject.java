package com.hadii.striff.parse;

import com.hadii.clarpse.compiler.ClarpseProject;
import com.hadii.clarpse.compiler.SourceFiles;
import com.hadii.clarpse.sourcemodel.OOPSourceCodeModel;

public class ParsedProject {

    private final SourceFiles codebase;

    public ParsedProject(SourceFiles codebase) {
        this.codebase = codebase;
    }

    public OOPSourceCodeModel model() throws Exception {
        return new ClarpseProject(codebase).result();
    }
}
