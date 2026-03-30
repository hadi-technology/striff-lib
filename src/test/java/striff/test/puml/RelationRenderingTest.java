package striff.test.puml;

import com.hadi.clarpse.compiler.ClarpseProject;
import com.hadi.clarpse.compiler.CompileException;
import com.hadi.clarpse.compiler.Lang;
import com.hadi.clarpse.compiler.ProjectFile;
import com.hadi.clarpse.compiler.ProjectFiles;
import com.hadi.clarpse.sourcemodel.OOPSourceCodeModel;
import com.hadi.striff.diagram.ComponentHelper;
import com.hadi.striff.diagram.DiagramComponent;
import com.hadi.striff.diagram.StriffDiagramModel;
import com.hadi.striff.diagram.display.DiagramDisplay;
import com.hadi.striff.diagram.display.LightDiagramColorScheme;
import com.hadi.striff.diagram.plantuml.PUMLDiagramData;
import com.hadi.striff.diagram.plantuml.PUMLDiagramText;
import com.hadi.striff.parse.CodeDiff;
import org.junit.Test;

import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class RelationRenderingTest {

    @Test
    public void relationsUseComponentIdsWithoutPackagePrefix() throws Exception {
        String plantUml = buildPlantUmlString();

        assertTrue(plantUml.contains("\"com-hadi-striff-StriffOperation\""));
        assertTrue(plantUml.contains("\"com-hadi-striff-StriffConfig\""));
        assertFalse(plantUml.contains("\"com.hadi.striff.com-hadi-striff-StriffOperation\""));
        assertFalse(plantUml.contains("\"com.hadi.striff.com-hadi-striff-StriffConfig\""));
    }

    private static String buildPlantUmlString() throws Exception {
        ProjectFiles oldFiles = new ProjectFiles();
        ProjectFiles newFiles = new ProjectFiles();
        newFiles.insertFile(new ProjectFile("/StriffConfig.java",
                "package com.hadi.striff; public class StriffConfig { }"));
        newFiles.insertFile(new ProjectFile("/StriffOperation.java",
                "package com.hadi.striff; public class StriffOperation { StriffConfig cfg; }"));

        CodeDiff diff = codeDiff(oldFiles, newFiles);
        StriffDiagramModel model = new StriffDiagramModel(diff, Set.of());
        Set<DiagramComponent> diagramCmps = model.diagramCmps();
        DiagramDisplay display = new DiagramDisplay(new LightDiagramColorScheme(), cmpPkgs(diagramCmps));

        PUMLDiagramData data = new PUMLDiagramData(
                model.diagramRels(),
                diff.changeSet().addedRelations(),
                diff.changeSet().deletedRelations(),
                display,
                diff.mergedModel(),
                diff.changeSet().addedComponents(),
                diff.changeSet().deletedComponents(),
                diff.changeSet().modifiedComponents(),
                diagramCmps);

        return PUMLDiagramText.build(data);
    }

    private static CodeDiff codeDiff(ProjectFiles oldFiles, ProjectFiles newFiles) throws CompileException {
        OOPSourceCodeModel oldModel = new ClarpseProject(oldFiles, Lang.JAVA).result().model();
        OOPSourceCodeModel newModel = new ClarpseProject(newFiles, Lang.JAVA).result().model();
        return new CodeDiff(oldModel, newModel);
    }

    private static Set<String> cmpPkgs(Set<DiagramComponent> cmps) {
        return cmps.stream()
                .map(cmp -> ComponentHelper.packagePath(cmp.pkg()))
                .collect(Collectors.toSet());
    }
}
