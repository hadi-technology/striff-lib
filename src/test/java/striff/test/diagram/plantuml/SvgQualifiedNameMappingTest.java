package striff.test.diagram.plantuml;

import com.hadi.clarpse.compiler.ClarpseProject;
import com.hadi.clarpse.compiler.CompileException;
import com.hadi.clarpse.compiler.Lang;
import com.hadi.clarpse.compiler.ProjectFile;
import com.hadi.clarpse.compiler.ProjectFiles;
import com.hadi.striff.diagram.ComponentHelper;
import com.hadi.striff.diagram.DiagramComponent;
import com.hadi.striff.diagram.StriffDiagramModel;
import com.hadi.striff.diagram.display.DiagramDisplay;
import com.hadi.striff.diagram.display.LightDiagramColorScheme;
import com.hadi.striff.diagram.plantuml.PUMLDiagram;
import com.hadi.striff.diagram.plantuml.PUMLDiagramData;
import com.hadi.striff.parse.CodeDiff;
import org.junit.Test;

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.junit.Assert.assertTrue;

/**
 * Tests that verify the data-qualified-name attributes in the SVG match
 * the unique names of diagram components returned in the API.
 */
public class SvgQualifiedNameMappingTest {

    private static final Pattern DATA_QUALIFIED_NAME_PATTERN = Pattern.compile(
            "data-qualified-name=\"([^\"]+)\""
    );

    private static Set<String> extractDataQualifiedNames(String svg) {
        Set<String> names = new java.util.HashSet<>();
        Matcher matcher = DATA_QUALIFIED_NAME_PATTERN.matcher(svg);
        while (matcher.find()) {
            names.add(matcher.group(1));
        }
        return names;
    }

    private static Set<String> cmpPkgs(Set<DiagramComponent> cmps) {
        return cmps.stream()
                .map(cmp -> ComponentHelper.packagePath(cmp.pkg()))
                .collect(Collectors.toSet());
    }

    /**
     * Test for synthetic module - verifies data-qualified-name is set correctly.
     */
    @Test
    public void syntheticModuleHasCorrectDataQualifiedName() throws Exception {
        ProjectFiles oldFiles = new ProjectFiles();
        ProjectFiles newFiles = new ProjectFiles();
        newFiles.insertFile(new ProjectFile("/cron.py", "def topLevelFn(): pass"));

        CodeDiff diff = codeDiff(oldFiles, newFiles, Lang.PYTHON);

        StriffDiagramModel diagramModel = new StriffDiagramModel(diff, Set.of());
        Set<DiagramComponent> diagramCmps = diagramModel.diagramCmps();
        DiagramDisplay display = new DiagramDisplay(new LightDiagramColorScheme(), cmpPkgs(diagramCmps));

        PUMLDiagramData data = new PUMLDiagramData(
                diagramModel.diagramRels(),
                diff.changeSet().addedRelations(),
                diff.changeSet().deletedRelations(),
                display,
                diff.mergedModel(),
                diff.changeSet().addedComponents(),
                diff.changeSet().deletedComponents(),
                diff.changeSet().modifiedComponents(),
                diagramCmps);

        PUMLDiagram diagram = new PUMLDiagram(data);
        String svg = diagram.svgText();

        // Get unique names from diagram components (this is what the API returns)
        Set<String> apiNames = diagramModel.diagramCmps().stream()
                .map(DiagramComponent::uniqueName)
                .collect(Collectors.toSet());

        // Extract data-qualified-names from SVG
        Set<String> svgNames = extractDataQualifiedNames(svg);

        // Verify synthetic module has correct data-qualified-name
        assertTrue("Synthetic module 'module:cron' should have data-qualified-name attribute",
                svgNames.contains("module:cron"));

        // Verify all API component unique names appear in SVG data-qualified-name attributes
        assertTrue("All API components should be in SVG: API=" + apiNames + ", SVG=" + svgNames,
                svgNames.containsAll(apiNames));
    }

    /**
     * Test for synthetic module with a package - verifies data-qualified-name is set correctly.
     */
    @Test
    public void syntheticModuleWithPackageHasCorrectDataQualifiedName() throws Exception {
        ProjectFiles oldFiles = new ProjectFiles();
        ProjectFiles newFiles = new ProjectFiles();
        // File in a subdirectory (package "src")
        newFiles.insertFile(new ProjectFile("/src/cron.py", "def topLevelFn(): pass"));

        CodeDiff diff = codeDiff(oldFiles, newFiles, Lang.PYTHON);

        StriffDiagramModel diagramModel = new StriffDiagramModel(diff, Set.of());
        Set<DiagramComponent> diagramCmps = diagramModel.diagramCmps();
        DiagramDisplay display = new DiagramDisplay(new LightDiagramColorScheme(), cmpPkgs(diagramCmps));

        PUMLDiagramData data = new PUMLDiagramData(
                diagramModel.diagramRels(),
                diff.changeSet().addedRelations(),
                diff.changeSet().deletedRelations(),
                display,
                diff.mergedModel(),
                diff.changeSet().addedComponents(),
                diff.changeSet().deletedComponents(),
                diff.changeSet().modifiedComponents(),
                diagramCmps);

        PUMLDiagram diagram = new PUMLDiagram(data);
        String svg = diagram.svgText();

        // Get unique names from diagram components
        Set<String> apiNames = diagramModel.diagramCmps().stream()
                .map(DiagramComponent::uniqueName)
                .collect(Collectors.toSet());

        // Extract data-qualified-names from SVG
        Set<String> svgNames = extractDataQualifiedNames(svg);

        // Verify the synthetic module has correct data-qualified-name
        // For a file in package "src", the uniqueName is "src.module:cron"
        assertTrue("Synthetic module 'src.module:cron' should have data-qualified-name attribute. Got: " + svgNames,
                svgNames.contains("src.module:cron"));

        // Verify all API components are in SVG
        assertTrue("All API components should be in SVG: API=" + apiNames + ", SVG=" + svgNames,
                svgNames.containsAll(apiNames));
    }

    /**
     * Test for regular class - verifies data-qualified-name is set correctly.
     */
    @Test
    public void regularClassHasCorrectDataQualifiedName() throws Exception {
        ProjectFiles oldFiles = new ProjectFiles();
        ProjectFiles newFiles = new ProjectFiles();
        newFiles.insertFile(new ProjectFile("/com/example/MyClass.java",
                "package com.example; public class MyClass { int x; }"));

        CodeDiff diff = codeDiff(oldFiles, newFiles);

        StriffDiagramModel diagramModel = new StriffDiagramModel(diff, Set.of());
        Set<DiagramComponent> diagramCmps = diagramModel.diagramCmps();
        DiagramDisplay display = new DiagramDisplay(new LightDiagramColorScheme(), cmpPkgs(diagramCmps));

        PUMLDiagramData data = new PUMLDiagramData(
                diagramModel.diagramRels(),
                diff.changeSet().addedRelations(),
                diff.changeSet().deletedRelations(),
                display,
                diff.mergedModel(),
                diff.changeSet().addedComponents(),
                diff.changeSet().deletedComponents(),
                diff.changeSet().modifiedComponents(),
                diagramCmps);

        PUMLDiagram diagram = new PUMLDiagram(data);
        String svg = diagram.svgText();

        // Get unique names from diagram components
        Set<String> apiNames = diagramModel.diagramCmps().stream()
                .map(DiagramComponent::uniqueName)
                .collect(Collectors.toSet());

        // Extract data-qualified-names from SVG
        Set<String> svgNames = extractDataQualifiedNames(svg);

        // Verify the class has correct data-qualified-name
        assertTrue("Class 'com.example.MyClass' should have data-qualified-name attribute",
                svgNames.contains("com.example.MyClass"));

        // Verify all API components are in SVG
        assertTrue("All API components should be in SVG: API=" + apiNames + ", SVG=" + svgNames,
                svgNames.containsAll(apiNames));
    }

    private static CodeDiff codeDiff(ProjectFiles oldFiles, ProjectFiles newFiles, Lang lang) throws Exception {
        com.hadi.clarpse.sourcemodel.OOPSourceCodeModel oldModel =
                new ClarpseProject(oldFiles, lang).result().model();
        com.hadi.clarpse.sourcemodel.OOPSourceCodeModel newModel =
                new ClarpseProject(newFiles, lang).result().model();
        return new CodeDiff(oldModel, newModel);
    }

    private static CodeDiff codeDiff(ProjectFiles oldFiles, ProjectFiles newFiles) throws Exception {
        return codeDiff(oldFiles, newFiles, Lang.JAVA);
    }
}
