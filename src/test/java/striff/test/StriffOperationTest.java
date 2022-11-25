package striff.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static striff.test.TestUtil.githubProjectFiles;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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
    public void testStriffsRespectSizeLimit() throws Exception {
        String baseRepoOwner = "Zir0-93";
        String repoName = "junit5";
        int softMaxSizeLimit = 12;
        Lang language = Lang.JAVA;
        ProjectFiles oldFiles = githubProjectFiles(
            baseRepoOwner, repoName, "25d727a186a3151c6cf22619c989082cad39b543", language);
        ProjectFiles newFiles = githubProjectFiles(
            baseRepoOwner, repoName, "9743eb1808b3a991cfe672d9333d81b0f5fc1118", language);
        List<StriffDiagram> striffs = new StriffOperation(
            oldFiles, newFiles, new StriffConfig(
            softMaxSizeLimit)).result().diagrams();
        assertEquals(striffs.size(), 1);
        assertEquals(striffs.get(0).containedPkgs().size(), 14);
        assertEquals(33, striffs.get(0).size());
    }

    @Test
    public void testEmptySourceFilesFilter() throws Exception {
        final ProjectFile fileA = new ProjectFile("fileA", "public class ClassA {}");
        final ProjectFile fileB = new ProjectFile("fileB", "public class ClassB {}");
        final ProjectFiles oldFiles = new ProjectFiles(Lang.JAVA);
        oldFiles.insertFile(fileA);
        final ProjectFiles newFiles = new ProjectFiles(Lang.JAVA);
        newFiles.insertFile(fileA);
        newFiles.insertFile(fileB);
        List<StriffDiagram> striffs = new StriffOperation(
            oldFiles, newFiles,
            new StriffConfig(Collections.emptyList(), OutputMode.DEFAULT)).result().diagrams();
        assertFalse(striffs.isEmpty());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNonMatchingSourceFilesFilterException() throws Exception {
        final ProjectFile fileA = new ProjectFile("/xfw3/core/fileA.java", "public class ClassA " +
            "{}");
        final ProjectFile fileB = new ProjectFile("/xfw3/core/fileB.java", "public class ClassB {}");
        final ProjectFiles oldFiles = new ProjectFiles(Lang.JAVA);
        oldFiles.insertFile(fileA);
        final ProjectFiles newFiles = new ProjectFiles(Lang.JAVA);
        newFiles.insertFile(fileA);
        newFiles.insertFile(fileB);
        List<String> filesFilter = Collections.singletonList("/core/fileA.java");
        new StriffOperation(
            oldFiles, newFiles, new StriffConfig(filesFilter, OutputMode.DEFAULT)).result().diagrams();
    }

    @Test
    public void testMatchingSourceFilesFilter() throws Exception {
        final ProjectFile fileA = new ProjectFile("/xfw3/core/fileA.java", "public class ClassA " +
            "{}");
        final ProjectFile fileB = new ProjectFile("/xfw3/core/fileB.java", "public class ClassB {}");
        final ProjectFiles oldFiles = new ProjectFiles(Lang.JAVA);
        oldFiles.insertFile(fileA);
        final ProjectFiles newFiles = new ProjectFiles(Lang.JAVA);
        newFiles.insertFile(fileA);
        newFiles.insertFile(fileB);
        List<String> filesFilter = Arrays.asList(
            "/xfw3/core/fileA.java",
            "/xfw3/core/fileB.java"
        );
        List<StriffDiagram> striffs = new StriffOperation(
            oldFiles, newFiles, new StriffConfig(filesFilter, OutputMode.DEFAULT)).result().diagrams();
        assert(!striffs.isEmpty());
    }

    @Test(expected = NoStructuralChangesException.class)
    public void noChangesException() throws Exception {
        new StriffOperation(new ProjectFiles(Lang.GOLANG), new ProjectFiles(Lang.GOLANG),
                            new StriffConfig(1));
    }
}
