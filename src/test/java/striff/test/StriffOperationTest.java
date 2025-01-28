package striff.test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hadii.clarpse.compiler.CompileException;
import com.hadii.clarpse.compiler.Lang;
import com.hadii.clarpse.compiler.ProjectFile;
import com.hadii.clarpse.compiler.ProjectFiles;
import com.hadii.striff.StriffConfig;
import com.hadii.striff.StriffOperation;
import com.hadii.striff.diagram.StriffDiagram;
import com.hadii.striff.diagram.StriffOutput;
import com.hadii.striff.diagram.display.OutputMode;
import com.hadii.striff.diagram.plantuml.PUMLDrawException;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static striff.test.TestUtil.githubProjectFiles;

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

		List<StriffDiagram> striffs = new StriffOperation(oldFiles, newFiles)
				.result().diagrams();

		assertEquals(1, striffs.size());
		assertEquals(13, striffs.get(0).containedPkgs().size());
		assertEquals(25, striffs.get(0).size());
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

		List<StriffDiagram> striffs = new StriffOperation(oldFiles, newFiles,
				StriffConfig.create()
						.setOutputMode(OutputMode.DEFAULT)
						.setLanguages(List.of(Lang.GOLANG)))
				.result().diagrams();

		assertEquals(0, striffs.size());
	}

	@Test
	public void testKeepRelevantCompileErrorsOnly() throws PUMLDrawException, CompileException, IOException {
		final ProjectFile fileA = new ProjectFile("/fileA.java", "publicad ; classdaw ClassA {}");
		final ProjectFile fileB = new ProjectFile("/fileB.java", "public class ClassB {}");

		ProjectFiles oldFiles = new ProjectFiles();
		oldFiles.insertFile(fileA);
		oldFiles.insertFile(fileB);

		ProjectFiles newFiles = new ProjectFiles();
		newFiles.insertFile(fileA);
		newFiles.insertFile(fileB);

		Set<String> compileErrors = new StriffOperation(oldFiles, newFiles,
				StriffConfig.create()
						.setOutputMode(OutputMode.DEFAULT)
						.setLanguages(List.of(Lang.JAVA)))
				.result().compileWarnings();

		assertTrue(compileErrors.contains("/fileA.java"));
	}

	@Test
	public void testAnalyzeSpecifiedFilterFilesOnly() throws PUMLDrawException, CompileException, IOException {
		final ProjectFile fileA = new ProjectFile("/fileA.java", "publicad ; classdaw ClassA {}");
		final ProjectFile fileB = new ProjectFile("/fileB.java", "public class ClassB {}");

		ProjectFiles oldFiles = new ProjectFiles();
		oldFiles.insertFile(fileA);
		oldFiles.insertFile(fileB);

		ProjectFiles newFiles = new ProjectFiles();
		newFiles.insertFile(fileA);
		newFiles.insertFile(fileB);

		Set<String> compileErrors = new StriffOperation(oldFiles, newFiles,
				StriffConfig.create()
						.setOutputMode(OutputMode.DEFAULT)
						.setFilesFilter(List.of("/fileB.java")))
				.result().compileWarnings();

		assertTrue(compileErrors.isEmpty());
	}

	@Test
	public void testEmptySourceFilesFilterShowsAllDifferences() throws Exception {
		final ProjectFile fileA = new ProjectFile("/fileA.java", "public class ClassA {}");
		final ProjectFile fileB = new ProjectFile("/fileB.java", "public class ClassB {}");

		ProjectFiles oldFiles = new ProjectFiles();
		oldFiles.insertFile(fileA);

		ProjectFiles newFiles = new ProjectFiles();
		newFiles.insertFile(fileA);
		newFiles.insertFile(fileB);

		List<StriffDiagram> striffs = new StriffOperation(oldFiles, newFiles,
				StriffConfig.create()
						.setOutputMode(OutputMode.DEFAULT))
				.result().diagrams();

		assertFalse(striffs.isEmpty());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testNonMatchingSourceFilesFilterException() throws Exception {
		final ProjectFile fileA = new ProjectFile("/xfw3/core/fileA.java", "public class ClassA {}");
		final ProjectFile fileB = new ProjectFile("/xfw3/core/fileB.java", "public class ClassB {}");

		ProjectFiles oldFiles = new ProjectFiles();
		oldFiles.insertFile(fileA);

		ProjectFiles newFiles = new ProjectFiles();
		newFiles.insertFile(fileA);
		newFiles.insertFile(fileB);

		List<String> filesFilter = Collections.singletonList("/core/fileA.java");

		new StriffOperation(oldFiles, newFiles,
				StriffConfig.create()
						.setOutputMode(OutputMode.DEFAULT)
						.setFilesFilter(filesFilter))
				.result().diagrams();
	}

	@Test
	public void testMatchingSourceFilesFilter() throws Exception {
		final ProjectFile fileA = new ProjectFile("/xfw3/core/fileA.java", "public class ClassA {}");
		final ProjectFile fileB = new ProjectFile("/xfw3/core/fileB.java", "public class ClassB {}");

		ProjectFiles oldFiles = new ProjectFiles();
		oldFiles.insertFile(fileA);

		ProjectFiles newFiles = new ProjectFiles();
		newFiles.insertFile(fileA);
		newFiles.insertFile(fileB);

		List<String> filesFilter = Arrays.asList(
				"/xfw3/core/fileA.java",
				"/xfw3/core/fileB.java");

		List<StriffDiagram> striffs = new StriffOperation(oldFiles, newFiles,
				StriffConfig.create()
						.setOutputMode(OutputMode.DEFAULT)
						.setFilesFilter(filesFilter))
				.result().diagrams();

		assertFalse(striffs.isEmpty());
	}

	@Test
	public void testStriffOutputSerialization() throws Exception {
		String baseRepoOwner = "Zir0-93";
		String repoName = "junit5";
		Lang language = Lang.JAVA;

		ProjectFiles oldFiles = githubProjectFiles(
				baseRepoOwner, repoName, "25d727a186a3151c6cf22619c989082cad39b543", language);
		ProjectFiles newFiles = githubProjectFiles(
				baseRepoOwner, repoName, "9743eb1808b3a991cfe672d9333d81b0f5fc1118", language);

		StriffOutput striffs = new StriffOperation(oldFiles, newFiles,
				StriffConfig.create()
						.setOutputMode(OutputMode.DEFAULT)
						.setLanguages(List.of(Lang.JAVA)))
				.result();
		ObjectMapper mapper = new ObjectMapper();
		String json = mapper.writeValueAsString(striffs);
		JsonNode root = mapper.readTree(json);
		assert root.has("compileWarnings");
		assert root.has("diagrams") && root.get("diagrams").isArray();
		JsonNode diagrams = root.get("diagrams");
		for (JsonNode diagram : diagrams) {
			assert diagram.has("packages") && diagram.get("packages").isArray();
			assert diagram.has("relations") && diagram.get("relations").isObject();
			assert diagram.has("changeSet") && diagram.get("changeSet").isObject();
			assert diagram.has("size") && diagram.get("size").isInt();
			assert diagram.has("compressedSVG") && diagram.get("compressedSVG").isTextual();
			assert diagram.has("components") && diagram.get("components").isArray();
		}
	}

	@Test
	public void testSVGCompression() throws PUMLDrawException, CompileException, IOException {
		final ProjectFile fileA = new ProjectFile("/xfw3/core/fileA.java", "public class ClassA {}");
		final ProjectFile fileB = new ProjectFile("/xfw3/core/fileB.java", "public class ClassB {}");

		ProjectFiles oldFiles = new ProjectFiles();
		oldFiles.insertFile(fileA);

		ProjectFiles newFiles = new ProjectFiles();
		newFiles.insertFile(fileA);
		newFiles.insertFile(fileB);

		List<String> filesFilter = Arrays.asList(
				"/xfw3/core/fileA.java",
				"/xfw3/core/fileB.java");
		StriffOutput striffs = new StriffOperation(oldFiles, newFiles,
				StriffConfig.create()
						.setOutputMode(OutputMode.DEFAULT)
						.setFilesFilter(filesFilter))
				.result();
		StriffDiagram diagram = striffs.diagrams().get(0);
		assertTrue(diagram.svg().equals(StriffDiagram.decompressAndDecodeSvg(diagram.compressedSVG())));
	}
}
