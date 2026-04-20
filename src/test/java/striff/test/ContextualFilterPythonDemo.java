package striff.test;

import com.hadi.clarpse.compiler.Lang;
import com.hadi.clarpse.compiler.ProjectFiles;
import com.hadi.striff.StriffConfig;
import com.hadi.striff.StriffOperation;
import com.hadi.striff.diagram.StriffDiagram;
import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class ContextualFilterPythonDemo {

    @Test
    public void testPythonContextualComponents() throws Exception {
        String emailServicePy = """
            class EmailService:
                def send_email(self, to, subject):
                    pass
            """;

        String userServiceOldPy = """
            class UserService:
                def __init__(self, name):
                    self.name = name
                def get_name(self):
                    return self.name
            """;

        String userServiceNewPy = """
            class UserService:
                def __init__(self, name):
                    self.name = name
                def get_name(self):
                    return self.name
                def send_welcome(self, email_service):
                    email_service.send_email(self.email, "Welcome!")
            """;

        Files.createDirectories(Paths.get("/tmp/py-old"));
        Files.createDirectories(Paths.get("/tmp/py-new"));

        Files.write(Paths.get("/tmp/py-old/email_service.py"), emailServicePy.getBytes());
        Files.write(Paths.get("/tmp/py-old/user_service.py"), userServiceOldPy.getBytes());

        Files.write(Paths.get("/tmp/py-new/email_service.py"), emailServicePy.getBytes());
        Files.write(Paths.get("/tmp/py-new/user_service.py"), userServiceNewPy.getBytes());

        ProjectFiles oldFiles = new ProjectFiles("/tmp/py-old");
        ProjectFiles newFiles = new ProjectFiles("/tmp/py-new");

        // Filter ONLY user_service.py
        StriffConfig config = new StriffConfig().setFilesFilter(List.of(
                "/tmp/py-new/user_service.py"));

        StriffOperation operation = new StriffOperation(oldFiles, newFiles, config);
        List<StriffDiagram> striffs = operation.result().diagrams();

        System.out.println("\n=== ChangeSet Info ===");
        System.out.println("Added components: " + operation.codeDiff().changeSet().addedComponents().size());
        operation.codeDiff().changeSet().addedComponents().forEach(c -> System.out.println("  + " + c));
        System.out.println("Modified components: " + operation.codeDiff().changeSet().modifiedComponents().size());
        operation.codeDiff().changeSet().modifiedComponents().forEach(c -> System.out.println("  ~ " + c));
        System.out.println("Key relations components: " + operation.codeDiff().changeSet().keyRelationsComponents().size());
        operation.codeDiff().changeSet().keyRelationsComponents().forEach(c -> System.out.println("  * " + c));

        System.out.println("\n=== Diagram Components ===");
        for (StriffDiagram striff : striffs) {
            System.out.println("Total components: " + striff.cmps().size());
            striff.cmps().forEach(c -> {
                String source = c.sourceFile() != null ? c.sourceFile() : "no-source";
                System.out.println("  - " + c.uniqueName() + " (source: " + source + ")");
            });
        }

        for (int i = 0; i < striffs.size(); i++) {
            Files.write(Paths.get("/tmp/striff-py-filtered-" + i + ".svg"), striffs.get(i).svg().getBytes());
            System.out.println("\nSVG " + i + ": file:///tmp/striff-py-filtered-" + i + ".svg");
        }
    }
}
