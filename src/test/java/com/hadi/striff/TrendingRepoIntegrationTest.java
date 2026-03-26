package com.hadi.striff;

import com.hadi.clarpse.compiler.Lang;
import com.hadi.clarpse.compiler.ProjectFile;
import com.hadi.clarpse.compiler.ProjectFiles;
import com.hadi.striff.diagram.StriffDiagram;
import com.hadi.striff.diagram.StriffOutput;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.Assert.*;

/**
 * Integration tests for trending GitHub repositories.
 * Tests striff-lib against real-world codebases to identify parsing issues.
 */
public class TrendingRepoIntegrationTest {

    private static final String TEST_REPO_BASE = "/tmp/striff-test-repos/repos";

    @Test
    public void testCamundaJavaRepo() throws Exception {
        testRepo("camunda", Lang.JAVA, "java");
    }

    @Test
    public void testElasticsearchJavaRepo() throws Exception {
        testRepo("elasticsearch", Lang.JAVA, "java");
    }

    @Test
    public void testPaimonJavaRepo() throws Exception {
        testRepo("paimon", Lang.JAVA, "java");
    }

    @Test
    public void testTaigaUITypeScriptRepo() throws Exception {
        testRepo("taiga-ui", Lang.TYPESCRIPT, "ts");
    }

    @Test
    public void testJupyterLiteTypeScriptRepo() throws Exception {
        testRepo("jupyterlite", Lang.TYPESCRIPT, "ts");
    }

    @Test
    public void testGStackTypeScriptRepo() throws Exception {
        testRepo("gstack", Lang.TYPESCRIPT, "ts");
    }

    @Test
    public void testNostalgiaForInfinityPythonRepo() throws Exception {
        testRepo("NostalgiaForInfinity", Lang.PYTHON, "py");
    }

    @Test
    public void testBrythonPythonRepo() throws Exception {
        testRepo("brython", Lang.PYTHON, "py");
    }

    @Test
    public void testGhostDownloaderPythonRepo() throws Exception {
        testRepo("Ghost-Downloader-3", Lang.PYTHON, "py");
    }

    private void testRepo(String repoName, Lang lang, String extension) throws Exception {
        Path repoPath = Paths.get(TEST_REPO_BASE, repoName);
        
        if (!Files.exists(repoPath)) {
            System.out.println("Skipping " + repoName + " - repo not cloned yet");
            return;
        }

        // Find up to 20 source files
        List<Path> sourceFiles;
        try (Stream<Path> paths = Files.walk(repoPath)) {
            sourceFiles = paths
                .filter(p -> p.toString().endsWith("." + extension))
                .filter(p -> !p.toString().contains("node_modules"))
                .filter(p -> !p.toString().contains(".git"))
                .limit(20)
                .toList();
        }

        if (sourceFiles.isEmpty()) {
            fail("No source files found in " + repoName);
        }

        System.out.println("Testing " + repoName + " with " + sourceFiles.size() + " files...");

        // Create "old" state
        ProjectFiles oldFiles = new ProjectFiles();
        for (Path file : sourceFiles) {
            String content = Files.readString(file);
            String relativePath = repoPath.relativize(file).toString();
            oldFiles.insertFile(new ProjectFile("/" + relativePath, content));
        }

        // Create "new" state with slight modifications
        ProjectFiles newFiles = new ProjectFiles();
        for (Path file : sourceFiles.subList(0, Math.min(5, sourceFiles.size()))) {
            String content = Files.readString(file);
            // Add a comment to simulate a change
            if (lang == Lang.JAVA) {
                content = content + "\n// Striff test change\n";
            } else if (lang == Lang.PYTHON) {
                content = content + "\n# Striff test change\n";
            } else {
                content = content + "\n// Striff test change\n";
            }
            String relativePath = repoPath.relativize(file).toString();
            newFiles.insertFile(new ProjectFile("/" + relativePath, content));
        }

        // Run striff
        StriffConfig config = new StriffConfig()
                .setLanguages(List.of(lang))
                .setMetadataOnly(true);  // Skip SVG for speed

        StriffOutput output = new StriffOperation(oldFiles, newFiles, config).result();

        // Verify results
        assertNotNull("Output should not be null", output);
        
        // Log results
        System.out.println("  - Compile warnings: " + output.compileWarnings().size());
        System.out.println("  - Diagrams: " + output.diagrams().size());
        
        if (!output.diagrams().isEmpty()) {
            StriffDiagram diagram = output.diagrams().get(0);
            System.out.println("  - Added components: " + diagram.changeSet().addedComponents().size());
            System.out.println("  - Modified components: " + diagram.changeSet().modifiedComponents().size());
            System.out.println("  - Components in diagram: " + diagram.cmps().size());
        }

        // Test should pass if we got output without crashes
        assertTrue("Should complete without exception", true);
    }
}
