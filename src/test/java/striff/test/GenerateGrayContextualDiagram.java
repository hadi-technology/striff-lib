package striff.test;

import com.hadi.clarpse.compiler.ProjectFiles;
import com.hadi.striff.StriffConfig;
import com.hadi.striff.StriffOperation;
import com.hadi.striff.diagram.StriffDiagram;
import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

/**
 * Generates a striff diagram showing contextual (gray) components.
 *
 * Contextual components are components that are NOT in the source file filter
 * but are referenced by modified components. They appear grayed out.
 */
public class GenerateGrayContextualDiagram {

    @Test
    public void generateGrayContextualDiagram() throws Exception {
        // Old codebase: 3 related classes in different files
        String userOld = """
            package com.example.model;
            import com.example.service.EmailService;
            import com.example.service.NotificationService;
            public class User {
                private String name;
                private EmailService emailService;
                public User(String name) {
                    this.name = name;
                    this.emailService = new EmailService();
                }
                public String getName() { return name; }
            }
            """;

        String emailService = """
            package com.example.service;
            public class EmailService {
                public void sendEmail(String to, String subject) {
                    // Send email logic
                }
            }
            """;

        String notificationService = """
            package com.example.service;
            public class NotificationService {
                public void notify(String message) {
                    // Notification logic
                }
            }
            """;

        // New codebase: User is modified, services unchanged
        String userNew = """
            package com.example.model;
            import com.example.service.EmailService;
            import com.example.service.NotificationService;
            public class User {
                private String name;
                private String email;
                private EmailService emailService;
                private NotificationService notificationService;

                public User(String name, String email) {
                    this.name = name;
                    this.email = email;
                    this.emailService = new EmailService();
                    this.notificationService = new NotificationService();
                }

                public String getName() { return name; }
                public void sendWelcome() {
                    emailService.sendEmail(email, "Welcome!");
                    notificationService.notify("User created: " + name);
                }
            }
            """;

        // Create old codebase
        Files.createDirectories(Paths.get("/tmp/gray-old/model"));
        Files.createDirectories(Paths.get("/tmp/gray-old/service"));
        Files.write(Paths.get("/tmp/gray-old/model/User.java"), userOld.getBytes());
        Files.write(Paths.get("/tmp/gray-old/service/EmailService.java"), emailService.getBytes());
        Files.write(Paths.get("/tmp/gray-old/service/NotificationService.java"), notificationService.getBytes());

        // Create new codebase
        Files.createDirectories(Paths.get("/tmp/gray-new/model"));
        Files.createDirectories(Paths.get("/tmp/gray-new/service"));
        Files.write(Paths.get("/tmp/gray-new/model/User.java"), userNew.getBytes());
        Files.write(Paths.get("/tmp/gray-new/service/EmailService.java"), emailService.getBytes());
        Files.write(Paths.get("/tmp/gray-new/service/NotificationService.java"), notificationService.getBytes());

        ProjectFiles oldFiles = new ProjectFiles("/tmp/gray-old");
        ProjectFiles newFiles = new ProjectFiles("/tmp/gray-new");

        // Filter to only User.java - this makes EmailService and NotificationService "contextual"
        StriffConfig config = new StriffConfig().setFilesFilter(List.of(
                "/tmp/gray-new/model/User.java"));

        List<StriffDiagram> striffs = new StriffOperation(oldFiles, newFiles, config)
                .result().diagrams();

        System.out.println("Generated " + striffs.size() + " diagram(s)");
        System.out.println("Components in the diagram:");
        for (StriffDiagram striff : striffs) {
            striff.cmps().forEach(c -> System.out.println("  - " + c.uniqueName()));
        }

        for (int i = 0; i < striffs.size(); i++) {
            String path = "/tmp/striff-gray-contextual-" + i + ".svg";
            Files.write(Paths.get(path), striffs.get(i).svg().getBytes());
            System.out.println("Diagram " + i + ": file://" + path);
        }
    }
}
