package striff.test.model;

import com.hadii.clarpse.compiler.Lang;
import com.hadii.clarpse.compiler.ProjectFiles;
import com.hadii.striff.StriffConfig;
import com.hadii.striff.StriffOperation;
import com.hadii.striff.diagram.StriffDiagram;
import com.hadii.striff.diagram.display.DiagramColorScheme;
import com.hadii.striff.diagram.display.DiagramColorSchemeOverride;
import com.hadii.striff.diagram.display.LightDiagramColorScheme;
import org.junit.Ignore;
import org.junit.Test;

import java.util.List;

import static striff.test.TestUtil.githubProjectFiles;
import static striff.test.TestUtil.writeStriffsToDisk;

public class StriffAPITest {

	/**
	 * The following test demonstrates how to generate striff diagrams for two
	 * versions of a code base located locally on disk. First replace the
	 * `ProjectFiles`
	 * paths with valid ones pointing to the two versions of your source code, then
	 * remove the @Ignore annotation and run the test. Striff diagrams representing
	 * the
	 * architectural difference between the two code bases will be outputted as SVG
	 * diagrams in the /tmp directory.
	 */
	@Ignore
	@Test
	public void testDemonstrateStriffAPI() throws Exception {
		// Note, a ProjectFiles instance can be instantiated with a path to a dir, zip
		// file, or ZipInputStream representing your source code.
		ProjectFiles originalCode = new ProjectFiles("/path/to/original/code");
		ProjectFiles modifiedCode = new ProjectFiles("/path/to/modified/code");
		List<StriffDiagram> striffs = new StriffOperation(
				originalCode, modifiedCode, new StriffConfig()).result().diagrams();
		System.out.println("Total diagrams generated: " + striffs.size());
		writeStriffsToDisk(striffs);
	}

	/**
	 * Generates striffs based on a Pull Request in GitHub. Ensure
	 * the source code refs exist and are still available before running.
	 */
	@Ignore
	@Test
	public void testDemonstrateStriffAPIWithPR() throws Exception {
		String baseRepoOwner = "hadii-tech";
		String repoName = "striff-lib";
		Lang language = Lang.JAVA;
		ProjectFiles oldFiles = githubProjectFiles(
				baseRepoOwner, repoName, "master", language);
		ProjectFiles newFiles = githubProjectFiles(
				baseRepoOwner, repoName, "tmp-changes", language);
		List<StriffDiagram> striffs = new StriffOperation(
				oldFiles, newFiles, new StriffConfig()).result().diagrams();
		System.out.println("Total diagrams generated: " + striffs.size());
		writeStriffsToDisk(striffs);
	}

	/**
	 * Demonstrates how to customize the diagram color scheme by overriding only
	 * selected fields.
	 */
	@Ignore
	@Test
	public void testDemonstrateColorSchemeOverride() throws Exception {
		ProjectFiles originalCode = new ProjectFiles("/path/to/original/code");
		ProjectFiles modifiedCode = new ProjectFiles("/path/to/modified/code");

		DiagramColorScheme scheme = DiagramColorSchemeOverride
				.from(new LightDiagramColorScheme())
				.setClassFontColor("#123456")
				.setPackageFontName("Courier New");

		StriffConfig config = new StriffConfig()
				.setColorScheme(scheme);

		List<StriffDiagram> striffs = new StriffOperation(
				originalCode, modifiedCode, config).result().diagrams();
		System.out.println("Total diagrams generated: " + striffs.size());
		writeStriffsToDisk(striffs);
	}

	/**
	 * Demonstrates how to filter analysis to a specific set of files.
	 */
	@Ignore
	@Test
	public void testDemonstrateFilesFilter() throws Exception {
		ProjectFiles originalCode = new ProjectFiles("/path/to/original/code");
		ProjectFiles modifiedCode = new ProjectFiles("/path/to/modified/code");

		StriffConfig config = new StriffConfig()
				.setFilesFilter(List.of(
						"/path/to/original/code/src/main/java/com/acme/Foo.java",
						"/path/to/original/code/src/main/java/com/acme/Bar.java"));

		List<StriffDiagram> striffs = new StriffOperation(
				originalCode, modifiedCode, config).result().diagrams();
		System.out.println("Total diagrams generated: " + striffs.size());
		writeStriffsToDisk(striffs);
	}
}
