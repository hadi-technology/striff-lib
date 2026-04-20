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
 * Generates a sample striff diagram to /tmp/striff-demo.svg
 */
public class GenerateDiagram {

    @Test
    public void generateSampleDiagram() throws Exception {
        String oldCode = """
            package com.example;
            public class UserService {
                private String name;
                public void create(String n) { name = n; }
            }
            """;
        String newCode = """
            package com.example;
            public class UserService {
                private String name;
                private String email;
                public void create(String n, String e) {
                    name = n;
                    email = e;
                }
                public void delete() { name = null; }
            }
            """;

        Files.createDirectories(Paths.get("/tmp/striff-old"));
        Files.createDirectories(Paths.get("/tmp/striff-new"));
        Files.write(Paths.get("/tmp/striff-old/UserService.java"), oldCode.getBytes());
        Files.write(Paths.get("/tmp/striff-new/UserService.java"), newCode.getBytes());

        ProjectFiles oldFiles = new ProjectFiles("/tmp/striff-old");
        ProjectFiles newFiles = new ProjectFiles("/tmp/striff-new");

        List<StriffDiagram> striffs = new StriffOperation(oldFiles, newFiles, new StriffConfig())
                .result().diagrams();

        for (int i = 0; i < striffs.size(); i++) {
            Files.write(Paths.get("/tmp/striff-diagram-" + i + ".svg"),
                    striffs.get(i).svg().getBytes());
        }

        System.out.println("Generated " + striffs.size() + " diagram(s) at:");
        System.out.println("file:///tmp/striff-diagram-0.svg");
    }
}
