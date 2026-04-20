package com.hadi.striff.diagram.plantuml;

import com.hadi.clarpse.compiler.ClarpseProject;
import com.hadi.clarpse.compiler.Lang;
import com.hadi.clarpse.compiler.ProjectFile;
import com.hadi.clarpse.compiler.ProjectFiles;
import com.hadi.clarpse.sourcemodel.OOPSourceCodeModel;
import com.hadi.clarpse.sourcemodel.OOPSourceModelConstants;
import com.hadi.striff.diagram.DiagramComponent;
import com.hadi.striff.diagram.display.DiagramDisplay;
import com.hadi.striff.diagram.display.LightDiagramColorScheme;
import com.hadi.striff.extractor.RelationsMap;
import org.junit.Test;

import java.util.Set;

import static org.junit.Assert.assertTrue;

public class PUMLClassChangeSummaryTest {

    private static final String PACKAGE = "com.test";
    private static final String CLASS_NAME = PACKAGE + ".ClassA";
    private static final String CLASS_CODE = ""
            + "package com.test;\n"
            + "public class ClassA {\n"
            + "    public String Name;\n"
            + "    private int Count;\n"
            + "    public void doThing() {}\n"
            + "}\n";

    @Test
    public void rendersChangeSummaryInClassHeader() throws Exception {
        ProjectFiles rawData = new ProjectFiles();
        rawData.insertFile(new ProjectFile("/ClassA.java", CLASS_CODE));
        ClarpseProject parseService = new ClarpseProject(rawData, Lang.JAVA);
        OOPSourceCodeModel codeModel = parseService.result().model();

        DiagramComponent classComponent = new DiagramComponent(codeModel.getComponent(CLASS_NAME).get(), codeModel);
        DiagramComponent firstField = null;
        DiagramComponent secondField = null;
        DiagramComponent method = null;

        for (String childName : classComponent.children()) {
            DiagramComponent child = new DiagramComponent(codeModel.getComponent(childName).get(), codeModel);
            if (child.componentType() == OOPSourceModelConstants.ComponentType.FIELD) {
                if (firstField == null) {
                    firstField = child;
                } else if (secondField == null) {
                    secondField = child;
                }
            } else if (child.componentType() == OOPSourceModelConstants.ComponentType.METHOD) {
                if (method == null) {
                    method = child;
                }
            }
        }

        assertTrue(firstField != null);
        assertTrue(secondField != null);
        assertTrue(method != null);

        DiagramDisplay display = new DiagramDisplay(new LightDiagramColorScheme(), Set.of());
        PUMLDiagramData data = new PUMLDiagramData(
                new RelationsMap(),
                new RelationsMap(),
                new RelationsMap(),
                display,
                codeModel,
                Set.of(firstField.uniqueName()),
                Set.of(secondField.uniqueName()),
                Set.of(method.uniqueName()),
                Set.of(classComponent));

        String puml = new PUMLClassFieldsCode(data).value(Set.of(classComponent));
        String normalized = puml.replaceAll("\\s+", " ");
        String expected = "ClassA [ <color:#bef5cb>+1</color> <color:#fdaeb7>-1</color> <color:#b3e3f5>+1</color>]";
        assertTrue(normalized.contains(expected));
    }

    @Test
    public void omitsZeroChangeSummary() throws Exception {
        ProjectFiles rawData = new ProjectFiles();
        rawData.insertFile(new ProjectFile("/ClassA.java", CLASS_CODE));
        ClarpseProject parseService = new ClarpseProject(rawData, Lang.JAVA);
        OOPSourceCodeModel codeModel = parseService.result().model();

        DiagramComponent classComponent = new DiagramComponent(codeModel.getComponent(CLASS_NAME).get(), codeModel);
        DiagramDisplay display = new DiagramDisplay(new LightDiagramColorScheme(), Set.of());
        PUMLDiagramData data = new PUMLDiagramData(
                new RelationsMap(),
                new RelationsMap(),
                new RelationsMap(),
                display,
                codeModel,
                Set.of(),
                Set.of(),
                Set.of(),
                Set.of(classComponent));

        String puml = new PUMLClassFieldsCode(data).value(Set.of(classComponent));
        assertTrue(!puml.contains("+0</color>"));
    }

    @Test
    public void omitsZeroModifiedSummary() throws Exception {
        ProjectFiles rawData = new ProjectFiles();
        rawData.insertFile(new ProjectFile("/ClassA.java", CLASS_CODE));
        ClarpseProject parseService = new ClarpseProject(rawData, Lang.JAVA);
        OOPSourceCodeModel codeModel = parseService.result().model();

        DiagramComponent classComponent = new DiagramComponent(codeModel.getComponent(CLASS_NAME).get(), codeModel);
        DiagramComponent firstField = null;
        DiagramComponent secondField = null;

        for (String childName : classComponent.children()) {
            DiagramComponent child = new DiagramComponent(codeModel.getComponent(childName).get(), codeModel);
            if (child.componentType() == OOPSourceModelConstants.ComponentType.FIELD) {
                if (firstField == null) {
                    firstField = child;
                } else if (secondField == null) {
                    secondField = child;
                }
            }
        }

        DiagramDisplay display = new DiagramDisplay(new LightDiagramColorScheme(), Set.of());
        PUMLDiagramData data = new PUMLDiagramData(
                new RelationsMap(),
                new RelationsMap(),
                new RelationsMap(),
                display,
                codeModel,
                Set.of(firstField.uniqueName()),
                Set.of(secondField.uniqueName()),
                Set.of(),
                Set.of(classComponent));

        String puml = new PUMLClassFieldsCode(data).value(Set.of(classComponent));
        assertTrue(!puml.contains("#b3e3f5"));
    }

    @Test
    public void colorsContextualBaseComponentGrayWhenOutsideSourceFileFilter() throws Exception {
        ProjectFiles rawData = new ProjectFiles();
        rawData.insertFile(new ProjectFile("/ClassA.java", CLASS_CODE));
        ClarpseProject parseService = new ClarpseProject(rawData, Lang.JAVA);
        OOPSourceCodeModel codeModel = parseService.result().model();

        DiagramComponent classComponent = new DiagramComponent(codeModel.getComponent(CLASS_NAME).get(), codeModel);
        DiagramDisplay display = new DiagramDisplay(new LightDiagramColorScheme(), Set.of());
        PUMLDiagramData data = new PUMLDiagramData(
                new RelationsMap(),
                new RelationsMap(),
                new RelationsMap(),
                display,
                codeModel,
                Set.of(),
                Set.of(),
                Set.of(),
                Set.of(classComponent),
                LayoutEngine.GRAPHVIZ,
                Set.of("/Other.java"));

        String puml = new PUMLClassFieldsCode(data).value(Set.of(classComponent));
        assertTrue(puml.contains("#back:b8b8b8"));
    }
}
