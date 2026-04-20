package striff.test;

import com.hadi.clarpse.compiler.ProjectFiles;
import com.hadi.striff.StriffConfig;
import com.hadi.striff.StriffOperation;
import com.hadi.striff.diagram.StriffDiagram;
import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class SvgDemo {
    @Test
    public void generateDemoSvg() throws Exception {
        String emailService = """
            package com.example.service;
            public class EmailService {
                public void sendEmail(String to, String subject) {}
            }
            """;

        String userServiceOld = """
            package com.example.service;
            public class UserService {
                private String name;
                public UserService(String n) { this.name = n; }
                public String getName() { return name; }
            }
            """;

        String userServiceNew = """
            package com.example.service;
            public class UserService {
                private String name;
                private String email;
                private EmailService emailService;
                public UserService(String n, String e) {
                    this.name = n;
                    this.email = e;
                    this.emailService = new EmailService();
                }
                public String getName() { return name; }
                public void sendWelcome() {
                    emailService.sendEmail(email, "Welcome!");
                }
            }
            """;

        Files.createDirectories(Paths.get("/tmp/demo-old"));
        Files.createDirectories(Paths.get("/tmp/demo-new"));

        Files.write(Paths.get("/tmp/demo-old/EmailService.java"), emailService.getBytes());
        Files.write(Paths.get("/tmp/demo-old/UserService.java"), userServiceOld.getBytes());

        Files.write(Paths.get("/tmp/demo-new/EmailService.java"), emailService.getBytes());
        Files.write(Paths.get("/tmp/demo-new/UserService.java"), userServiceNew.getBytes());

        ProjectFiles oldFiles = new ProjectFiles("/tmp/demo-old");
        ProjectFiles newFiles = new ProjectFiles("/tmp/demo-new");

        List<StriffDiagram> striffs = new StriffOperation(oldFiles, newFiles, new StriffConfig())
                .result().diagrams();

        System.out.println("Generated " + striffs.size() + " diagram(s)");
        for (StriffDiagram striff : striffs) {
            System.out.println("Components: " + striff.cmps().size());
            striff.cmps().forEach(c -> System.out.println("  - " + c.uniqueName()));
        }

        for (int i = 0; i < striffs.size(); i++) {
            Files.write(Paths.get("/tmp/striff-demo-" + i + ".svg"), striffs.get(i).svg().getBytes());
            System.out.println("SVG " + i + ": file:///tmp/striff-demo-" + i + ".svg");
        }
    }
}
