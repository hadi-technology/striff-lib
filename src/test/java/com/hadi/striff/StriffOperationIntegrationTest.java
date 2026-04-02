package com.hadi.striff;

import com.hadi.clarpse.compiler.Lang;
import com.hadi.clarpse.compiler.ProjectFile;
import com.hadi.clarpse.compiler.ProjectFiles;
import com.hadi.striff.diagram.StriffDiagram;
import com.hadi.striff.diagram.StriffOutput;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * End-to-end integration tests for {@link StriffOperation}.
 * These tests feed small ProjectFile snippets through the full pipeline
 * and validate the output diagrams, change sets, and SVG content.
 */
public class StriffOperationIntegrationTest {

    @Test
    public void endToEndJavaClassAddition() throws Exception {
        // Old code: single class
        ProjectFiles oldFiles = new ProjectFiles();
        oldFiles.insertFile(new ProjectFile("/ClassA.java",
                "package com.sample; public class ClassA {}"));

        // New code: ClassA now depends on a new ClassB
        ProjectFiles newFiles = new ProjectFiles();
        newFiles.insertFile(new ProjectFile("/ClassA.java",
                "package com.sample; public class ClassA { private ClassB b; }"));
        newFiles.insertFile(new ProjectFile("/ClassB.java",
                "package com.sample; public class ClassB {}"));

        StriffConfig config = new StriffConfig()
                .setLanguages(List.of(Lang.JAVA));
        StriffOutput output = new StriffOperation(oldFiles, newFiles, config).result();

        // Verify diagrams were generated
        assertFalse("Expected at least one diagram", output.diagrams().isEmpty());
        StriffDiagram diagram = output.diagrams().get(0);

        // Verify the change set detected the addition
        assertTrue("Expected ClassB in added components",
                diagram.changeSet().inAddedComponents("com.sample.ClassB"));

        // Verify SVG was rendered
        assertNotNull("SVG should be rendered", diagram.svg());
        assertTrue("SVG should contain ClassB", diagram.svg().contains("ClassB"));

        // Verify no compile warnings
        assertTrue("Expected no compile warnings", output.compileWarnings().isEmpty());
    }

    @Test
    public void endToEndJavaClassDeletion() throws Exception {
        // Old code: two classes
        ProjectFiles oldFiles = new ProjectFiles();
        oldFiles.insertFile(new ProjectFile("/ClassA.java",
                "package com.sample; public class ClassA { private ClassB b; }"));
        oldFiles.insertFile(new ProjectFile("/ClassB.java",
                "package com.sample; public class ClassB {}"));

        // New code: ClassB is removed
        ProjectFiles newFiles = new ProjectFiles();
        newFiles.insertFile(new ProjectFile("/ClassA.java",
                "package com.sample; public class ClassA { }"));

        StriffConfig config = new StriffConfig()
                .setLanguages(List.of(Lang.JAVA));
        StriffOutput output = new StriffOperation(oldFiles, newFiles, config).result();

        // Verify diagrams were generated
        assertFalse("Expected at least one diagram", output.diagrams().isEmpty());
        StriffDiagram diagram = output.diagrams().get(0);

        // Verify the change set detected the deletion
        assertTrue("Expected ClassB in deleted components",
                diagram.changeSet().inDeletedComponents("com.sample.ClassB"));

        // Verify SVG was rendered
        assertNotNull("SVG should be rendered", diagram.svg());

        // Verify no compile warnings
        assertTrue("Expected no compile warnings", output.compileWarnings().isEmpty());
    }

    @Test
    public void endToEndJavaClassModification() throws Exception {
        // Old code: simple class
        ProjectFiles oldFiles = new ProjectFiles();
        oldFiles.insertFile(new ProjectFile("/ClassA.java",
                "package com.sample; public class ClassA { }"));

        // New code: class has a new field
        ProjectFiles newFiles = new ProjectFiles();
        newFiles.insertFile(new ProjectFile("/ClassA.java",
                "package com.sample; public class ClassA { private int x; }"));

        StriffConfig config = new StriffConfig()
                .setLanguages(List.of(Lang.JAVA));
        StriffOutput output = new StriffOperation(oldFiles, newFiles, config).result();

        // Verify diagrams were generated
        assertFalse("Expected at least one diagram", output.diagrams().isEmpty());
        StriffDiagram diagram = output.diagrams().get(0);

        // Verify the change set detected the modification
        assertTrue("Expected ClassA in modified components",
                diagram.changeSet().modifiedComponents().contains("com.sample.ClassA"));

        // Verify SVG was rendered
        assertNotNull("SVG should be rendered", diagram.svg());
        assertTrue("SVG should contain ClassA", diagram.svg().contains("ClassA"));
    }

    @Test
    public void endToEndEmptyProjects() throws Exception {
        // Both projects are empty
        ProjectFiles oldFiles = new ProjectFiles();
        ProjectFiles newFiles = new ProjectFiles();

        StriffConfig config = new StriffConfig()
                .setLanguages(List.of(Lang.JAVA));
        StriffOutput output = new StriffOperation(oldFiles, newFiles, config).result();

        // Verify no diagrams were generated for empty projects
        assertTrue("Expected no diagrams for empty projects", output.diagrams().isEmpty());

        // Verify no compile warnings
        assertTrue("Expected no compile warnings", output.compileWarnings().isEmpty());
    }

    // Note: Python and TypeScript tests require additional setup (tsconfig.json, etc.)
    // and are tested separately in other test classes.
    // These integration tests focus on the core Java end-to-end flow.

    @Test
    public void endToEndMetadataOnlyOutput() throws Exception {
        // Test metadata-only mode (skip SVG rendering)
        ProjectFiles oldFiles = new ProjectFiles();
        oldFiles.insertFile(new ProjectFile("/ClassA.java",
                "package com.sample; public class ClassA { }"));

        ProjectFiles newFiles = new ProjectFiles();
        newFiles.insertFile(new ProjectFile("/ClassA.java",
                "package com.sample; public class ClassA { private int x; }"));

        StriffConfig config = new StriffConfig()
                .setLanguages(List.of(Lang.JAVA))
                .setMetadataOnly(true);
        StriffOutput output = new StriffOperation(oldFiles, newFiles, config).result();

        // Verify diagrams were generated
        assertFalse("Expected at least one diagram", output.diagrams().isEmpty());
        StriffDiagram diagram = output.diagrams().get(0);

        // Verify SVG is null (metadata only mode)
        assertNull("SVG should be null in metadata-only mode", diagram.svg());

        // But metadata should still be available
        assertTrue("Expected ClassA in modified components",
                diagram.changeSet().modifiedComponents().contains("com.sample.ClassA"));
        assertFalse("Expected components", diagram.cmps().isEmpty());
    }

    @Test
    public void rejectsInvalidFileFilterPaths() {
        ProjectFiles oldFiles = new ProjectFiles();
        oldFiles.insertFile(new ProjectFile("/ClassA.java",
                "package com.sample; public class ClassA { }"));

        ProjectFiles newFiles = new ProjectFiles();
        newFiles.insertFile(new ProjectFile("/ClassA.java",
                "package com.sample; public class ClassA { }"));

        StriffConfig config = new StriffConfig()
                .setLanguages(List.of(Lang.JAVA))
                .setFilesFilter(List.of("/Missing.java"));

        assertThrows(IllegalArgumentException.class,
                () -> new StriffOperation(oldFiles, newFiles, config));
    }

    @Test
    public void maxComponentsPerDiagramCapLimitsRenderedComponents() throws Exception {
        ProjectFiles oldFiles = new ProjectFiles();
        ProjectFiles newFiles = new ProjectFiles();
        newFiles.insertFile(new ProjectFile("/ClassA.java",
                "package com.sample; public class ClassA { }"));
        newFiles.insertFile(new ProjectFile("/ClassB.java",
                "package com.sample; public class ClassB { }"));

        StriffConfig config = new StriffConfig()
                .setLanguages(List.of(Lang.JAVA))
                .setMaxComponentsPerDiagram(1);

        StriffOutput output = new StriffOperation(oldFiles, newFiles, config).result();

        assertFalse("Expected at least one diagram", output.diagrams().isEmpty());
        StriffDiagram diagram = output.diagrams().get(0);
        assertTrue("Expected metadata to still include all selected components",
                diagram.cmps().size() > 1);
        assertNull("Expected SVG rendering to be skipped when the component cap is exceeded", diagram.svg());
    }
}
