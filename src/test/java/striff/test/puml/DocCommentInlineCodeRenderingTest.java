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
import com.hadi.striff.diagram.plantuml.LayoutEngine;
import com.hadi.striff.diagram.plantuml.PUMLDiagram;
import com.hadi.striff.diagram.plantuml.PUMLDiagramData;
import com.hadi.striff.diagram.plantuml.PUMLDiagramText;
import com.hadi.striff.parse.CodeDiff;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.Assert.assertTrue;

public class DocCommentInlineCodeRenderingTest {

    @Test
    public void writesDiagramWithInlineCodeCommentToDisk() throws Exception {
        PUMLDiagramData data = buildDiagramData();
        String svg = new PUMLDiagram(data).svgText();
        String puml = PUMLDiagramText.build(data);
        Path outputDir = Paths.get(System.getProperty("java.io.tmpdir"), "striffs");
        Files.createDirectories(outputDir);
        Files.writeString(outputDir.resolve("inline-code-comment.svg"), svg, StandardCharsets.UTF_8);
        Files.writeString(outputDir.resolve("inline-code-comment.puml"), puml, StandardCharsets.UTF_8);

        assertTrue(svg.contains("safeText"));
        assertTrue(svg.contains("render[]"));
    }

    private static PUMLDiagramData buildDiagramData() throws Exception {
        ProjectFiles oldFiles = new ProjectFiles();
        ProjectFiles newFiles = new ProjectFiles();
        newFiles.insertFile(new ProjectFile("/Example.java",
                "package demo.inlinecode;\n"
                        + "/** Uses `safeText` before calling `render()` in the final diagram. */\n"
                        + "public class Example { }\n"));

        CodeDiff diff = codeDiff(oldFiles, newFiles);
        StriffDiagramModel model = new StriffDiagramModel(diff, Set.of());
        Set<DiagramComponent> diagramComponents = model.diagramCmps();
        DiagramDisplay display = new DiagramDisplay(
                new LightDiagramColorScheme(),
                diagramComponents.stream()
                        .map(DiagramComponent::pkg)
                        .map(ComponentHelper::packagePath)
                        .collect(Collectors.toSet()));

        return new PUMLDiagramData(
                model.diagramRels(),
                diff.changeSet().addedRelations(),
                diff.changeSet().deletedRelations(),
                display,
                diff.mergedModel(),
                diff.changeSet().addedComponents(),
                diff.changeSet().deletedComponents(),
                diff.changeSet().modifiedComponents(),
                diagramComponents,
                LayoutEngine.SMETANA);
    }

    private static CodeDiff codeDiff(ProjectFiles oldFiles, ProjectFiles newFiles) throws CompileException {
        OOPSourceCodeModel oldModel = new ClarpseProject(oldFiles, Lang.JAVA).result().model();
        OOPSourceCodeModel newModel = new ClarpseProject(newFiles, Lang.JAVA).result().model();
        return new CodeDiff(oldModel, newModel);
    }
}
