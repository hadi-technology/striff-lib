package striff.test.diagram.plantuml;

import com.hadi.clarpse.compiler.ClarpseProject;
import com.hadi.clarpse.compiler.Lang;
import com.hadi.clarpse.compiler.ProjectFile;
import com.hadi.clarpse.compiler.ProjectFiles;
import com.hadi.clarpse.sourcemodel.OOPSourceCodeModel;
import com.hadi.striff.diagram.DiagramComponent;
import com.hadi.striff.diagram.StriffDiagramModel;
import com.hadi.striff.diagram.plantuml.SvgAttributeAugmenter;
import com.hadi.striff.parse.CodeDiff;
import org.junit.Test;

import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SvgAttributeAugmenterTest {

    @Test
    public void addsQualifiedNameAttributeToMatchingEntityGroup() throws Exception {
        Set<DiagramComponent> components = components("package com.example; public class Example { }");
        String svg = "<svg><g id=\"ent0001\" class=\"entity\"><text>Example</text></g></svg>";

        String augmented = SvgAttributeAugmenter.addQualifiedNameAttributes(svg, components);

        assertTrue(augmented.contains("data-qualified-name=\"com.example.Example\""));
    }

    @Test
    public void returnsInputWhenSvgOrComponentsMissing() {
        assertEquals("", SvgAttributeAugmenter.addQualifiedNameAttributes("", Set.of()));
        assertEquals("<svg/>", SvgAttributeAugmenter.addQualifiedNameAttributes("<svg/>", Set.of()));
    }

    private static Set<DiagramComponent> components(String source) throws Exception {
        ProjectFiles oldFiles = new ProjectFiles();
        ProjectFiles newFiles = new ProjectFiles();
        newFiles.insertFile(new ProjectFile("/Example.java", source));
        OOPSourceCodeModel oldModel = new ClarpseProject(oldFiles, Lang.JAVA).result().model();
        OOPSourceCodeModel newModel = new ClarpseProject(newFiles, Lang.JAVA).result().model();
        return new StriffDiagramModel(new CodeDiff(oldModel, newModel), Set.of()).diagramCmps();
    }
}
