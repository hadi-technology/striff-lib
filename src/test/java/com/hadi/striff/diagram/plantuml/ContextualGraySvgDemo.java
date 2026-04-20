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
 * Generates SVG showing contextual gray components.
 */
public class ContextualGraySvgDemo {

    private static final String SERVICE_CODE = """
        package com.example.service;
        public class EmailService {
            public void sendEmail(String to, String subject) {}
        }
        """;

    private static final String USER_SERVICE_CODE = """
        package com.example.service;
        public class UserService {
            private EmailService emailService;
            public UserService() {
                this.emailService = new EmailService();
            }
            public void createUser(String name) {
                emailService.sendEmail(name, "Welcome!");
            }
        }
        """;

    @Test
    public void generateSvgWithGrayContextualComponent() throws Exception {
        ProjectFiles rawData = new ProjectFiles();
        rawData.insertFile(new ProjectFile("/EmailService.java", SERVICE_CODE));
        rawData.insertFile(new ProjectFile("/UserService.java", USER_SERVICE_CODE));
        ClarpseProject parseService = new ClarpseProject(rawData, Lang.JAVA);
        OOPSourceCodeModel codeModel = parseService.result().model();

        DiagramComponent userService = new DiagramComponent(
                codeModel.getComponent("com.example.service.UserService").get(), codeModel);
        DiagramComponent emailService = new DiagramComponent(
                codeModel.getComponent("com.example.service.EmailService").get(), codeModel);

        // Create display with filter = only UserService.java
        // This means EmailService will be gray (contextual)
        DiagramDisplay display = new DiagramDisplay(new LightDiagramColorScheme(), Set.of());

        PUMLDiagramData data = new PUMLDiagramData(
                new RelationsMap(), new RelationsMap(), new RelationsMap(),
                display, codeModel,
                Set.of(), Set.of(), Set.of(),
                Set.of(userService, emailService),
                LayoutEngine.GRAPHVIZ,
                Set.of("/UserService.java"));  // Filter: only UserService

        // Generate PUML class diagram code
        PUMLClassDiagramCode diagramCode = new PUMLClassDiagramCode(data);
        String puml = diagramCode.code();

        System.out.println("\n=== Full PlantUML ===");
        System.out.println(puml);

        // Generate SVG
        PUMLDiagram diagram = new PUMLDiagram(data);
        String svg = diagram.svgText();

        Files.write(Paths.get("/tmp/striff-contextual-gray.puml"), puml.getBytes());
        Files.write(Paths.get("/tmp/striff-contextual-gray.svg"), svg.getBytes());

        System.out.println("\nFiles saved:");
        System.out.println("  PUML: file:///tmp/striff-contextual-gray.puml");
        System.out.println("  SVG:  file:///tmp/striff-contextual-gray.svg");

        // Check styling
        if (puml.contains("EmailService.*#back:b8b8b8")) {
            System.out.println("\n✓ EmailService has GRAY background in PUML");
        }
    }
}
