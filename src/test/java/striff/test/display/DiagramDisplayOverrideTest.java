package striff.test.display;

import com.hadii.clarpse.compiler.ClarpseProject;
import com.hadii.clarpse.compiler.CompileException;
import com.hadii.clarpse.compiler.Lang;
import com.hadii.clarpse.compiler.ProjectFile;
import com.hadii.clarpse.compiler.ProjectFiles;
import com.hadii.clarpse.sourcemodel.OOPSourceCodeModel;
import com.hadii.striff.diagram.ComponentHelper;
import com.hadii.striff.diagram.DiagramComponent;
import com.hadii.striff.diagram.StriffDiagramModel;
import com.hadii.striff.diagram.display.DiagramDisplay;
import com.hadii.striff.diagram.display.DiagramDisplayOverride;
import com.hadii.striff.diagram.display.LightDiagramColorScheme;
import com.hadii.striff.diagram.plantuml.PUMLDiagramData;
import com.hadii.striff.diagram.plantuml.PUMLDiagramText;
import com.hadii.striff.parse.CodeDiff;
import org.junit.Test;

import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class DiagramDisplayOverrideTest {

    @Test
    public void overrideUpdatesOnlySpecifiedValues() throws Exception {
        CodeDiff diff = codeDiff();
        StriffDiagramModel model = new StriffDiagramModel(diff, Set.of());
        Set<DiagramComponent> diagramCmps = model.diagramCmps();
        Set<String> pkgs = cmpPkgs(diagramCmps);

        DiagramDisplay baseDisplay = new DiagramDisplay(new LightDiagramColorScheme(), pkgs);
        String baseOutput = buildPlantUmlString(baseDisplay, model, diff, diagramCmps);

        DiagramDisplayOverride override = new DiagramDisplayOverride()
                .setClassFontColor("#123456");
        DiagramDisplay mergedDisplay = baseDisplay.merge(override);
        String mergedOutput = buildPlantUmlString(mergedDisplay, model, diff, diagramCmps);

        assertFalse(baseOutput.contains("#123456"));
        assertTrue(mergedOutput.contains("#123456"));
        assertTrue(mergedOutput.contains(new LightDiagramColorScheme().classBorderColor()));

        assertEquals(baseOutput, buildPlantUmlString(baseDisplay.merge(null), model, diff, diagramCmps));
    }

    private static CodeDiff codeDiff() throws CompileException {
        ProjectFiles oldFiles = new ProjectFiles();
        ProjectFiles newFiles = new ProjectFiles();
        newFiles.insertFile(new ProjectFile("/A.java", "public class A { int x; }"));

        OOPSourceCodeModel oldModel = new ClarpseProject(oldFiles, Lang.JAVA).result().model();
        OOPSourceCodeModel newModel = new ClarpseProject(newFiles, Lang.JAVA).result().model();
        return new CodeDiff(oldModel, newModel);
    }

    private static Set<String> cmpPkgs(Set<DiagramComponent> cmps) {
        return cmps.stream()
                .map(cmp -> ComponentHelper.packagePath(cmp.pkg()))
                .collect(Collectors.toSet());
    }

    private static String buildPlantUmlString(DiagramDisplay display, StriffDiagramModel model,
            CodeDiff diff, Set<DiagramComponent> diagramCmps) {
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
}
