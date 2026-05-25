package com.hadi.striff.diagram.plantuml;

import com.hadi.clarpse.compiler.ClarpseProject;
import com.hadi.clarpse.compiler.Lang;
import com.hadi.clarpse.compiler.ProjectFile;
import com.hadi.clarpse.compiler.ProjectFiles;
import com.hadi.clarpse.sourcemodel.OOPSourceCodeModel;
import com.hadi.striff.diagram.DiagramComponent;
import com.hadi.striff.diagram.display.DiagramDisplay;
import com.hadi.striff.diagram.display.LightDiagramColorScheme;
import com.hadi.striff.extractor.RelationsMap;
import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Set;

/**
 * Demo: Contextual gray component styling.
 *
 * When a component's source file is NOT in the sourceFilesFilter passed to
 * PUMLDiagramData, the component is rendered with gray background (#b8b8b8).
 */
public class ContextualGrayDemo {

    private static final String CLASS_CODE = ""
            + "package com.test;\n"
            + "public class UserService {\n"
            + "    public String name;\n"
            + "    private int count;\n"
            + "    public void doThing() {}\n"
            + "}\n";

    @Test
    public void demonstrateContextualGrayStyling() throws Exception {
        ProjectFiles rawData = new ProjectFiles();
        rawData.insertFile(new ProjectFile("/UserService.java", CLASS_CODE));
        ClarpseProject parseService = new ClarpseProject(rawData, Lang.JAVA);
        OOPSourceCodeModel codeModel = parseService.result().model();

        DiagramComponent classComponent = new DiagramComponent(
                codeModel.getComponent("com.test.UserService").get(), codeModel);

        System.out.println("Component sourceFile: [" + classComponent.sourceFile() + "]");
        System.out.println("Filter: Set.of(\"/OtherFile.java\")");
        System.out.println("Is /OtherFile.java equal to /UserService.java? " + "/OtherFile.java".equals(classComponent.sourceFile()));

        // Normal styling (no filter)
        DiagramDisplay normalDisplay = new DiagramDisplay(new LightDiagramColorScheme(), Set.of());
        PUMLDiagramData normalData = new PUMLDiagramData(
                new RelationsMap(), new RelationsMap(), new RelationsMap(),
                normalDisplay, codeModel,
                Set.of(), Set.of(), Set.of(), Set.of(classComponent));
        String normalPuml = new PUMLClassFieldsCode(normalData).value(Set.of(classComponent));

        // Contextual gray styling - filter is /OtherFile.java, component is /UserService.java
        DiagramDisplay filteredDisplay = new DiagramDisplay(new LightDiagramColorScheme(), Set.of());
        PUMLDiagramData grayData = new PUMLDiagramData(
                new RelationsMap(), new RelationsMap(), new RelationsMap(),
                filteredDisplay, codeModel,
                Set.of(), Set.of(), Set.of(), Set.of(classComponent),
                LayoutEngine.GRAPHVIZ,
                Set.of("/OtherFile.java"), Set.of());  // KEY: sourceFilesFilter parameter
        String grayPuml = new PUMLClassFieldsCode(grayData).value(Set.of(classComponent));

        System.out.println("\n=== Normal PUML (no filter) ===");
        System.out.println(normalPuml);

        System.out.println("\n=== Contextual Gray PUML (filter=/OtherFile.java) ===");
        System.out.println(grayPuml);

        if (grayPuml.contains("#back:b8b8b8")) {
            System.out.println("\n✓ SUCCESS: Gray background (#b8b8b8) applied!");
        } else {
            System.out.println("\n✗ FAIL: Gray background not found");
        }

        Files.write(Paths.get("/tmp/striff-contextual-gray.puml"), grayPuml.getBytes());
        System.out.println("\nPUML file: file:///tmp/striff-contextual-gray.puml");
    }
}
