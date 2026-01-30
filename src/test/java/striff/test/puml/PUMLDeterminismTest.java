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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class PUMLDeterminismTest {

    @Test
    public void testPlantUmlTextDeterministicForSameInput() throws Exception {
        String first = buildPlantUmlString();
        String second = buildPlantUmlString();

        assertEquals(first, second);
        assertTrue(first.contains("@startuml"));
        assertTrue(first.contains("\"A\""));
        assertTrue(first.contains("\"B\""));
    }

    private static String buildPlantUmlString() throws Exception {
        ProjectFiles oldFiles = new ProjectFiles();
        oldFiles.insertFile(new ProjectFile("/A.java", "public class A { int x; }"));

        ProjectFiles newFiles = new ProjectFiles();
        newFiles.insertFile(new ProjectFile("/A.java", "public class A { int x; void m() {} }"));
        newFiles.insertFile(new ProjectFile("/B.java", "public class B { }"));

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
