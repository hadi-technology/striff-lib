package com.hadi.striff.diagram.plantuml;

import com.hadi.clarpse.compiler.Lang;
import com.hadi.clarpse.compiler.ProjectFile;
import com.hadi.clarpse.compiler.ProjectFiles;
import com.hadi.clarpse.compiler.typescript.NodeRuntime;
import com.hadi.striff.StriffConfig;
import com.hadi.striff.StriffOperation;
import com.hadi.striff.diagram.StriffDiagram;
import org.junit.Assume;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

/** The identifier a component is drawn under, for names that hold what PlantUML reads as syntax. */
public class DiagramIdentifierTest {

    @Test
    public void aNameThatDrawsIsWrittenAsItAlwaysWas() {
        assertEquals("com-acme-Ledger", PUMLHelper.pumlId("com.acme.Ledger"));
        assertEquals("com-acme-Ledger-post-Entry-", PUMLHelper.pumlId("com.acme.Ledger.post(Entry)"));
        assertEquals("com-acme-Box", PUMLHelper.pumlId("com.acme.Box<T>"));
        assertEquals("apps-app-[orgId]-page-Page", PUMLHelper.pumlId("apps.app.[orgId].page.Page"));
        assertEquals("src-$store-@types-user_model", PUMLHelper.pumlId("src.$store.@types.user_model"));
    }

    @Test
    public void whatPlantUmlReadsAsSyntaxIsWrittenOut() {
        for (String name : List.of("template.{{cookiecutter.project_slug}}.app.user.UserService",
                "my project.src.Main", "src.\"quoted\".Thing", "src.café.Menu", "a.b<c.D")) {
            String id = PUMLHelper.pumlId(name);
            assertTrue(id, id.matches("[\\x21-\\x7e]+"));
            for (char read : new char[] {'{', '}', '"', '<', '>', ' '}) {
                assertTrue(id, id.indexOf(read) < 0);
            }
        }
    }

    @Test
    public void twoNamesThatDifferOnlyInSuchACharacterAreTwoIdentifiers() {
        assertNotEquals(PUMLHelper.pumlId("src.{a}.Thing"), PUMLHelper.pumlId("src. a .Thing"));
        assertEquals(PUMLHelper.pumlId("src.{a}.Thing"), PUMLHelper.pumlId("src.{a}.Thing"));
    }

    @Test
    public void everyCharacterAnIdentifierKeepsDraws() throws Exception {
        for (char character = '!'; character < 127; character++) {
            String id = PUMLHelper.pumlId("app.a" + character + "b.Page");
            String svg = new String(PUMLHelper.generateDiagram("@startuml\npackage \"p\" as pkg_a {\n"
                    + "class " + id + " as \"Page\"\nclass other-T as \"T\"\n}\n\"" + id
                    + "\" --> \"other-T\"\n@enduml\n"), StandardCharsets.UTF_8);
            assertFalse("an identifier holding '" + character + "' does not draw: " + id,
                    PUMLHelper.invalidPUMLDiagram(svg));
        }
    }

    /** A project kept under a directory named by a template expression is drawn. */
    @Test
    public void aProjectUnderATemplateDirectoryIsDrawn() throws Exception {
        Assume.assumeTrue(NodeRuntime.isNodeAvailable());
        String directory = "/template/{{cookiecutter.project_slug}}/backend/app/services/";
        ProjectFiles before = new ProjectFiles();
        before.insertFile(new ProjectFile(directory + "user.py",
                "class UserService:\n    def find(self):\n        return None\n"));
        ProjectFiles after = new ProjectFiles();
        after.insertFile(new ProjectFile(directory + "user.py",
                "class UserService:\n    def find(self):\n        return None\n\n"
                + "    def save(self):\n        return None\n"));
        after.insertFile(new ProjectFile(directory + "audit.py",
                "from .user import UserService\n\n\nclass AuditService(UserService):\n"
                + "    def record(self):\n        return None\n"));

        StriffOperation operation = new StriffOperation(before, after,
                new StriffConfig().setLanguages(Set.of(Lang.PYTHON)));

        List<StriffDiagram> diagrams = operation.result().diagrams();
        assertFalse(diagrams.isEmpty());
        String svg = diagrams.get(0).svg();
        assertTrue(svg.contains("AuditService"));
        assertTrue(svg.contains("UserService"));
        assertFalse(PUMLHelper.invalidPUMLDiagram(svg));
    }
}
