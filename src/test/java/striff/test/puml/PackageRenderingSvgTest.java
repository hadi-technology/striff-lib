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

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class PackageRenderingSvgTest {

    private static final Pattern TEXT_PATTERN = Pattern.compile("<text[^>]*>([^<]+)</text>");

    @Test
    public void graphvizRendersDottedPackageAsSingleVisibleLabel() throws Exception {
        assertSingleVisiblePackageLabel(LayoutEngine.GRAPHVIZ);
    }

    @Test
    public void smetanaRendersDottedPackageAsSingleVisibleLabel() throws Exception {
        assertSingleVisiblePackageLabel(LayoutEngine.SMETANA);
    }

    @Test
    public void nestedPackagesRenderAsNestedBlocksInPlantUml() throws Exception {
        String plantUml = buildPlantUmlText();
        String rootBlock = packageBlock(plantUml, "com.hadi.striff");

        assertTrue(rootBlock.contains("package \"com.hadi.striff.diagram\" as "));
    }

    private static void assertSingleVisiblePackageLabel(LayoutEngine layoutEngine) throws Exception {
        String svg = buildSvg(layoutEngine);
        Set<String> visibleText = extractVisibleText(svg);

        assertTrue("Expected full package label in SVG text: " + visibleText,
                visibleText.contains("pkgRoot.pkgMid.pkgLeaf"));
        assertFalse("Should not render root package as a separate box label: " + visibleText,
                visibleText.contains("pkgRoot"));
        assertFalse("Should not render middle package as a separate box label: " + visibleText,
                visibleText.contains("pkgMid"));
        assertFalse("Should not render leaf package as a separate box label: " + visibleText,
                visibleText.contains("pkgLeaf"));
    }

    private static String buildSvg(LayoutEngine layoutEngine) throws Exception {
        PUMLDiagramData data = buildPackageData(layoutEngine,
                "package pkgRoot.pkgMid.pkgLeaf; public class A { }",
                "package pkgRoot.pkgMid.pkgLeaf; public class B { }");
        return new PUMLDiagram(data).svgText();
    }

    private static String buildPlantUmlText() throws Exception {
        PUMLDiagramData data = buildPackageData(LayoutEngine.SMETANA,
                "package com.hadi.striff; public class RootType { }",
                "package com.hadi.striff.diagram; public class ChildType { }");
        return PUMLDiagramText.build(data);
    }

    private static PUMLDiagramData buildPackageData(LayoutEngine layoutEngine, String... sources) throws Exception {
        ProjectFiles oldFiles = new ProjectFiles();

        ProjectFiles newFiles = new ProjectFiles();
        for (int i = 0; i < sources.length; i++) {
            newFiles.insertFile(new ProjectFile("/File" + i + ".java", sources[i]));
        }

        CodeDiff diff = codeDiff(oldFiles, newFiles);
        StriffDiagramModel model = new StriffDiagramModel(diff, Set.of());
        Set<DiagramComponent> diagramCmps = model.diagramCmps();
        DiagramDisplay display = new DiagramDisplay(new LightDiagramColorScheme(), cmpPkgs(diagramCmps));

        return new PUMLDiagramData(
                model.diagramRels(),
                diff.changeSet().addedRelations(),
                diff.changeSet().deletedRelations(),
                display,
                diff.mergedModel(),
                diff.changeSet().addedComponents(),
                diff.changeSet().deletedComponents(),
                diff.changeSet().modifiedComponents(),
                diagramCmps,
                layoutEngine);
    }

    private static Set<String> extractVisibleText(String svg) {
        Set<String> text = new HashSet<>();
        Matcher matcher = TEXT_PATTERN.matcher(svg);
        while (matcher.find()) {
            text.add(matcher.group(1));
        }
        return text;
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

    private static String packageBlock(String puml, String packageName) {
        String packageKeyword = "package \"" + packageName + "\"";
        int start = puml.indexOf(packageKeyword);
        if (start < 0) {
            throw new AssertionError("Package block not found for " + packageName);
        }
        int open = puml.indexOf('{', start);
        int depth = 1;
        for (int i = open + 1; i < puml.length(); i++) {
            char ch = puml.charAt(i);
            if (ch == '{') {
                depth++;
            } else if (ch == '}') {
                depth--;
                if (depth == 0) {
                    return puml.substring(start, i + 1);
                }
            }
        }
        throw new AssertionError("Unclosed package block for " + packageName);
    }
}
