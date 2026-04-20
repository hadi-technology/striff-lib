package striff.test;

import com.hadi.clarpse.compiler.ProjectFiles;
import com.hadi.striff.StriffConfig;
import com.hadi.striff.StriffOperation;
import com.hadi.striff.diagram.StriffDiagram;
import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class ContextualFilterDemo {

    @Test
    public void testKeyRelationsComponentsWithFilter() throws Exception {
        String emailService = """
            package com.example.service;
            public class EmailService {
                public void sendEmail(String to, String subject) {}
            }
            """;

        // UserService exists in both old and new, but gets NEW relation to EmailService
        String userServiceOld = """
            package com.example.service;
            public class UserService {
                private String name;
                public UserService(String n) { this.name = n; }
                public String getName() { return name; }
                public void doWork() {}
            }
            """;

        String userServiceNew = """
            package com.example.service;
            public class UserService {
                private String name;
                private EmailService emailService;  // NEW field = NEW relation
                public UserService(String n) { 
                    this.name = n; 
                    this.emailService = new EmailService();  // NEW usage
                }
                public String getName() { return name; }
                public void doWork() {}
            }
            """;

        Files.createDirectories(Paths.get("/tmp/filter-old"));
        Files.createDirectories(Paths.get("/tmp/filter-new"));

        Files.write(Paths.get("/tmp/filter-old/EmailService.java"), emailService.getBytes());
        Files.write(Paths.get("/tmp/filter-old/UserService.java"), userServiceOld.getBytes());

        Files.write(Paths.get("/tmp/filter-new/EmailService.java"), emailService.getBytes());
        Files.write(Paths.get("/tmp/filter-new/UserService.java"), userServiceNew.getBytes());

        ProjectFiles oldFiles = new ProjectFiles("/tmp/filter-old");
        ProjectFiles newFiles = new ProjectFiles("/tmp/filter-new");

        // Filter ONLY UserService.java
        StriffConfig config = new StriffConfig().setFilesFilter(List.of(
                "/tmp/filter-new/UserService.java"));

        StriffOperation operation = new StriffOperation(oldFiles, newFiles, config);
        List<StriffDiagram> striffs = operation.result().diagrams();

        System.out.println("\n=== ChangeSet Info ===");
        System.out.println("Added components: " + operation.codeDiff().changeSet().addedComponents().size());
        operation.codeDiff().changeSet().addedComponents().forEach(c -> System.out.println("  + " + c));
        System.out.println("Modified components: " + operation.codeDiff().changeSet().modifiedComponents().size());
        operation.codeDiff().changeSet().modifiedComponents().forEach(c -> System.out.println("  ~ " + c));
        System.out.println("Key relations components: " + operation.codeDiff().changeSet().keyRelationsComponents().size());
        operation.codeDiff().changeSet().keyRelationsComponents().forEach(c -> System.out.println("  * " + c));
        System.out.println("Added relations: " + operation.codeDiff().changeSet().addedRelations().size());

        System.out.println("\n=== Diagram Components ===");
        for (StriffDiagram striff : striffs) {
            System.out.println("Total components: " + striff.cmps().size());
            striff.cmps().forEach(c -> {
                String source = c.sourceFile() != null ? c.sourceFile() : "no-source";
                System.out.println("  - " + c.uniqueName() + " (source: " + source + ")");
            });
        }

        for (int i = 0; i < striffs.size(); i++) {
            Files.write(Paths.get("/tmp/striff-filtered-" + i + ".svg"), striffs.get(i).svg().getBytes());
            System.out.println("\nSVG " + i + ": file:///tmp/striff-filtered-" + i + ".svg");
        }

        boolean emailServiceShown = striffs.stream()
                .flatMap(s -> s.cmps().stream())
                .anyMatch(c -> c.uniqueName().equals("com.example.service.EmailService"));

        if (emailServiceShown) {
            System.out.println("\n✓ EmailService IS in the diagram");
        } else {
            System.out.println("\n✗ EmailService NOT in diagram");
        }
    }
}
