package striff.test;

import com.hadii.clarpse.compiler.Lang;
import com.hadii.clarpse.compiler.ProjectFiles;
import com.hadii.striff.StriffConfig;
import com.hadii.striff.StriffOperation;
import com.hadii.striff.diagram.StriffDiagram;
import com.hadii.striff.diagram.display.OutputMode;
import org.junit.Ignore;
import org.junit.Test;

import java.util.List;

import static striff.test.TestUtil.githubProjectFiles;
import static striff.test.TestUtil.writeStriffsToDisk;

public class StriffAPITest {

    /**
     * The following test demonstrates how to generate striff diagrams for two versions of a
     * code base located locally on disk. First replace the `ProjectFiles` paths with valid ones
     * pointing to the two versions of your source code, then remove the @Ignore annotation and
     * run the test. Striff diagrams representing the architectural difference between the two
     * code bases will be outputted as SVG diagrams in the /tmp directory.
     */
    @Ignore
    @Test
    public void testDemonstrateStriffAPI() throws Exception {
        // Note, a ProjectFiles instance can be instantiated with a path to a dir, zip file, or
        // ZipInputStream representing your source code.
        ProjectFiles originalCode = new ProjectFiles(Lang.JAVA, "/path/to/original/code");
        ProjectFiles modifiedCode = new ProjectFiles(Lang.JAVA, "/path/to/modified/code");
        List<StriffDiagram> striffs = new StriffOperation(
            originalCode, modifiedCode, new StriffConfig(OutputMode.DEFAULT)).result().diagrams();
        System.out.println("Total diagrams generated: " + striffs.size());
        writeStriffsToDisk(striffs, "sample-striffs");
    }

    /**
     * We can also generate striffs based on a Pull Request in GitHub. Ensure
     * the source code refs exist and are still available before running.
     */
    @Ignore
    @Test
    public void testDemonstrateStriffAPIWithPR() throws Exception {
        String baseRepoOwner = "junit-team";
        String repoName = "junit5";
        Lang language = Lang.JAVA;
        ProjectFiles oldFiles = githubProjectFiles(
            baseRepoOwner, repoName, "01353cddca2768d51f811ed464737a2087876e3e", language);
        ProjectFiles newFiles = githubProjectFiles(
            baseRepoOwner, repoName, "718b6937415dd825383641f9ddb00ce12f4640fb", language);
        List<StriffDiagram> striffs = new StriffOperation(
            oldFiles, newFiles, new StriffConfig(OutputMode.DEFAULT)).result().diagrams();
        System.out.println("Total diagrams generated: " + striffs.size());
        writeStriffsToDisk(striffs, "github-striffs-demo");
    }
}
