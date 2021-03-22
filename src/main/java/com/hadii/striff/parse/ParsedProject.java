package com.hadii.striff.parse;

import com.hadii.clarpse.compiler.ClarpseProject;
import com.hadii.clarpse.compiler.ProjectFiles;
import com.hadii.clarpse.sourcemodel.OOPSourceCodeModel;

public class ParsedProject {

    private final ProjectFiles codebase;

    public ParsedProject(ProjectFiles codebase) {
        this.codebase = codebase;
    }

    public OOPSourceCodeModel model() throws StriffParseException {
        try {
            return new ClarpseProject(codebase).result();
        } catch (Exception e) {
            e.printStackTrace();
            throw new StriffParseException("An error was encountered while parsing this "
                                                   + codebase.getLanguage() + " code base!");
        }
    }
}
