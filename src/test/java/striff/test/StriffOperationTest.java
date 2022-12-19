package striff.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static striff.test.TestUtil.githubProjectFiles;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.hadii.clarpse.compiler.CompileException;
import com.hadii.striff.diagram.plantuml.PUMLDrawException;
import org.junit.Test;

import com.hadii.clarpse.compiler.Lang;
import com.hadii.clarpse.compiler.ProjectFile;
import com.hadii.clarpse.compiler.ProjectFiles;
import com.hadii.striff.NoStructuralChangesException;
import com.hadii.striff.StriffConfig;
import com.hadii.striff.StriffOperation;
import com.hadii.striff.diagram.StriffDiagram;
import com.hadii.striff.diagram.display.OutputMode;

public class StriffOperationTest {

    @Test
    public void testBasicStriffOperation() throws Exception {
        String baseRepoOwner = "Zir0-93";
        String repoName = "junit5";
        Lang language = Lang.JAVA;
        ProjectFiles oldFiles = githubProjectFiles(
            baseRepoOwner, repoName, "25d727a186a3151c6cf22619c989082cad39b543", language);
        ProjectFiles newFiles = githubProjectFiles(
            baseRepoOwner, repoName, "9743eb1808b3a991cfe672d9333d81b0f5fc1118", language);
        List<StriffDiagram> striffs = new StriffOperation(
            oldFiles, newFiles).result().diagrams();
        assertEquals(striffs.size(), 1);
        assertEquals(striffs.get(0).containedPkgs().size(), 14);
        assertEquals(33, striffs.get(0).size());
    }

    @Test
    public void testLangSpecificStriffOperation() throws Exception {
        String baseRepoOwner = "Zir0-93";
        String repoName = "junit5";
        Lang language = Lang.JAVA;
        ProjectFiles oldFiles = githubProjectFiles(
            baseRepoOwner, repoName, "25d727a186a3151c6cf22619c989082cad39b543", language);
        ProjectFiles newFiles = githubProjectFiles(
            baseRepoOwner, repoName, "9743eb1808b3a991cfe672d9333d81b0f5fc1118", language);
        List<StriffDiagram> striffs = new StriffOperation(
            oldFiles, newFiles, new StriffConfig(OutputMode.DEFAULT,
                                                 List.of(Lang.GOLANG))).result().diagrams();
        assertEquals(striffs.size(), 0);
    }

    @Test
    public void testKeepRelevantCompileErrorsOnly() throws PUMLDrawException, CompileException, NoStructuralChangesException, IOException {
        final ProjectFile fileA = new ProjectFile("/fileA.java", "publicad ; classdaw ClassA {}");
        final ProjectFile fileB = new ProjectFile("/fileB.java", "public class ClassB {}");
        final ProjectFiles oldFiles = new ProjectFiles();
        oldFiles.insertFile(fileA);
        oldFiles.insertFile(fileB);
        final ProjectFiles newFiles = new ProjectFiles();
        newFiles.insertFile(fileA);
        newFiles.insertFile(fileB);
        Set<String> compileErrors = new StriffOperation(
            oldFiles, newFiles,
            new StriffConfig(OutputMode.DEFAULT, List.of(Lang.JAVA))).result().compileWarnings();
        // Empty files filter means changes across all files are included in generated striffs.
        assertTrue(compileErrors.contains("/fileA.java"));
    }

    @Test
    public void testAnalyzeSpecifiedFilterFilesOnly() throws PUMLDrawException, CompileException, NoStructuralChangesException, IOException {
        final ProjectFile fileA = new ProjectFile("/fileA.java", "publicad ; classdaw ClassA {}");
        final ProjectFile fileB = new ProjectFile("/fileB.java", "public class ClassB {}");
        final ProjectFiles oldFiles = new ProjectFiles();
        oldFiles.insertFile(fileA);
        oldFiles.insertFile(fileB);
        final ProjectFiles newFiles = new ProjectFiles();
        newFiles.insertFile(fileA);
        newFiles.insertFile(fileB);
        Set<String> compileErrors = new StriffOperation(
            oldFiles, newFiles,
            new StriffConfig(OutputMode.DEFAULT, List.of("/fileB.java"))).result().compileWarnings();
        // Empty files filter means changes across all files are included in generated striffs.
        assertTrue(compileErrors.isEmpty());
    }


    @Test
    public void testEmptySourceFilesFilterShowsAllDifferences() throws Exception {
        final ProjectFile fileA = new ProjectFile("/fileA.java", "public class ClassA {}");
        final ProjectFile fileB = new ProjectFile("/fileB.java", "public class ClassB {}");
        final ProjectFiles oldFiles = new ProjectFiles();
        oldFiles.insertFile(fileA);
        final ProjectFiles newFiles = new ProjectFiles();
        newFiles.insertFile(fileA);
        newFiles.insertFile(fileB);
        List<StriffDiagram> striffs = new StriffOperation(
            oldFiles, newFiles,
            new StriffConfig(OutputMode.DEFAULT)).result().diagrams();
        assertFalse(striffs.isEmpty());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNonMatchingSourceFilesFilterException() throws Exception {
        final ProjectFile fileA = new ProjectFile("/xfw3/core/fileA.java", "public class ClassA " +
            "{}");
        final ProjectFile fileB = new ProjectFile("/xfw3/core/fileB.java", "public class ClassB {}");
        final ProjectFiles oldFiles = new ProjectFiles();
        oldFiles.insertFile(fileA);
        final ProjectFiles newFiles = new ProjectFiles();
        newFiles.insertFile(fileA);
        newFiles.insertFile(fileB);
        List<String> filesFilter = Collections.singletonList("/core/fileA.java");
        new StriffOperation(
            oldFiles, newFiles,
            new StriffConfig(OutputMode.DEFAULT, filesFilter)).result().diagrams();
    }

    @Test
    public void testMatchingSourceFilesFilter() throws Exception {
        final ProjectFile fileA = new ProjectFile("/xfw3/core/fileA.java", "public class ClassA " +
            "{}");
        final ProjectFile fileB = new ProjectFile("/xfw3/core/fileB.java", "public class ClassB {}");
        final ProjectFiles oldFiles = new ProjectFiles();
        oldFiles.insertFile(fileA);
        final ProjectFiles newFiles = new ProjectFiles();
        newFiles.insertFile(fileA);
        newFiles.insertFile(fileB);
        List<String> filesFilter = Arrays.asList(
            "/xfw3/core/fileA.java",
            "/xfw3/core/fileB.java"
        );
        List<StriffDiagram> striffs = new StriffOperation(
            oldFiles, newFiles, new StriffConfig(
                OutputMode.DEFAULT, filesFilter)).result().diagrams();
        assert(!striffs.isEmpty());
    }
}
