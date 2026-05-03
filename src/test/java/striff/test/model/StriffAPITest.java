package striff.test.model;

import com.hadi.clarpse.compiler.Lang;
import com.hadi.clarpse.compiler.ProjectFiles;
import com.hadi.striff.StriffConfig;
import com.hadi.striff.StriffOperation;
import com.hadi.striff.diagram.StriffDiagram;
import com.hadi.striff.diagram.display.DiagramColorScheme;
import com.hadi.striff.diagram.display.DiagramColorSchemeOverride;
import com.hadi.striff.diagram.display.LightDiagramColorScheme;
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
		String baseRepoOwner = "fastapi";
		String repoName = "fastapi";
		Lang language = Lang.PYTHON;
		ProjectFiles oldFiles = githubProjectFiles(
				baseRepoOwner, repoName, "1f442c4", language);
		ProjectFiles newFiles = githubProjectFiles(
				baseRepoOwner, repoName, "pull/15269/head", language);
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

	/**
	 * Spring Boot PR #50095 - EndpointRequest links matcher fix.
	 * Merged PR, base=3.5.x, 4 changed files.
	 */
	@Ignore
	@Test
	public void testSpringBootEndpointRequest() throws Exception {
		String baseRepoOwner = "spring-projects";
		String repoName = "spring-boot";
		ProjectFiles oldFiles = githubProjectFiles(
				baseRepoOwner, repoName, "d2b62bc64f6a1e45e2f65293608c018a96e58971",
				Lang.JAVA);
		ProjectFiles newFiles = githubProjectFiles(
				baseRepoOwner, repoName, "pull/50095/head", Lang.JAVA);
		StriffConfig config = new StriffConfig()
				.setResolveContextualComponents(true);
		List<StriffDiagram> striffs = new StriffOperation(
				oldFiles, newFiles, config).result().diagrams();
		System.out.println("Spring Boot #50095 - Total diagrams: "
				+ striffs.size());
		writeStriffsToDisk(striffs, "spring-50095");
	}

	/**
	 * Spring Boot PR #50273 - Unwrap AOP proxies in configprops endpoint.
	 * Open PR, base=main, 2 changed files.
	 */
	@Ignore
	@Test
	public void testSpringBootAopProxy() throws Exception {
		String baseRepoOwner = "spring-projects";
		String repoName = "spring-boot";
		ProjectFiles oldFiles = githubProjectFiles(
				baseRepoOwner, repoName, "1a64cac622eb854974beb1d78edbc200a343b53e",
				Lang.JAVA);
		ProjectFiles newFiles = githubProjectFiles(
				baseRepoOwner, repoName, "pull/50273/head", Lang.JAVA);
		StriffConfig config = new StriffConfig()
				.setResolveContextualComponents(true);
		List<StriffDiagram> striffs = new StriffOperation(
				oldFiles, newFiles, config).result().diagrams();
		System.out.println("Spring Boot #50273 - Total diagrams: "
				+ striffs.size());
		writeStriffsToDisk(striffs, "spring-50273");
	}

	/**
	 * Langchain4j PR #5060 - AI Services: support polymorphic return types.
	 * Merged PR, base=main, 10 changed files.
	 */
	@Ignore
	@Test
	public void testLangchain4jPolymorphic() throws Exception {
		String baseRepoOwner = "langchain4j";
		String repoName = "langchain4j";
		ProjectFiles oldFiles = githubProjectFiles(
				baseRepoOwner, repoName, "03e755246b8576c8cd626f86de16915598f2bb29",
				Lang.JAVA);
		ProjectFiles newFiles = githubProjectFiles(
				baseRepoOwner, repoName, "pull/5060/head", Lang.JAVA);
		StriffConfig config = new StriffConfig()
				.setResolveContextualComponents(true);
		List<StriffDiagram> striffs = new StriffOperation(
				oldFiles, newFiles, config).result().diagrams();
		System.out.println("Langchain4j #5060 - Total diagrams: "
				+ striffs.size());
		writeStriffsToDisk(striffs, "langchain4j-5060");
	}
}
