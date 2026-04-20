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
 * Contextual components are not modified but are referenced by modified components.
 */
public class GenerateContextualDiagram {

    @Test
    public void generateContextualComponentDiagram() throws Exception {
        // Old codebase: 3 related classes
        String userOld = """
            package com.example.model;
            import com.example.service.EmailService;
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
                    // Send email
                }
            }
            """;

        String notificationService = """
            package com.example.service;
            public class NotificationService {
                public void notify(String message) {
                    // Send notification
                }
            }
            """;

        // New codebase: User is modified, EmailService and NotificationService unchanged
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
        Files.createDirectories(Paths.get("/tmp/context-old/model"));
        Files.createDirectories(Paths.get("/tmp/context-old/service"));
        Files.write(Paths.get("/tmp/context-old/model/User.java"), userOld.getBytes());
        Files.write(Paths.get("/tmp/context-old/service/EmailService.java"), emailService.getBytes());
        Files.write(Paths.get("/tmp/context-old/service/NotificationService.java"), notificationService.getBytes());

        // Create new codebase
        Files.createDirectories(Paths.get("/tmp/context-new/model"));
        Files.createDirectories(Paths.get("/tmp/context-new/service"));
        Files.write(Paths.get("/tmp/context-new/model/User.java"), userNew.getBytes());
        Files.write(Paths.get("/tmp/context-new/service/EmailService.java"), emailService.getBytes());
        Files.write(Paths.get("/tmp/context-new/service/NotificationService.java"), notificationService.getBytes());

        ProjectFiles oldFiles = new ProjectFiles("/tmp/context-old");
        ProjectFiles newFiles = new ProjectFiles("/tmp/context-new");

        List<StriffDiagram> striffs = new StriffOperation(oldFiles, newFiles, new StriffConfig())
                .result().diagrams();

        System.out.println("Generated " + striffs.size() + " diagram(s)");

        for (int i = 0; i < striffs.size(); i++) {
            String path = "/tmp/striff-contextual-" + i + ".svg";
            Files.write(Paths.get(path), striffs.get(i).svg().getBytes());
            System.out.println("Diagram " + i + ": file://" + path);
        }
    }
}
