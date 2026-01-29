package striff.test.puml;

import com.hadi.clarpse.compiler.CompileException;
import com.hadi.clarpse.compiler.Lang;
import com.hadi.clarpse.compiler.ProjectFile;
import com.hadi.clarpse.compiler.ProjectFiles;
import com.hadi.striff.StriffConfig;
import com.hadi.striff.StriffOperation;
import com.hadi.striff.diagram.StriffDiagram;
import com.hadi.striff.diagram.display.OutputMode;
import com.hadi.striff.diagram.plantuml.PUMLDrawException;
import com.hadi.striff.diagram.plantuml.PUMLHelper;
import org.junit.Test;

import java.io.IOException;
import java.util.List;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class PUMLTest {

	@Test
	public void testDetectPUMLError() throws PUMLDrawException, IOException {
		final String invalidPUMLString = " \n@startuml \n gotcha \n package { } } \n@enduml";
		final byte[] diagram = PUMLHelper.generateDiagram(invalidPUMLString);
		final String diagramStr = new String(diagram);
		assertTrue(PUMLHelper.invalidPUMLDiagram(diagramStr));
	}

	@Test
	public void testNoPumlErrorDetected() throws PUMLDrawException, IOException {
		final String validPUMLString = " \n@startuml \n package {} \n@enduml";
		final byte[] diagram = PUMLHelper.generateDiagram(validPUMLString);
		final String diagramStr = new String(diagram);
		assertFalse(PUMLHelper.invalidPUMLDiagram(diagramStr));
	}

	@Test
	public void testOnlyShowModifiedComponents() throws PUMLDrawException, IOException, CompileException {
		final ProjectFile fileA = new ProjectFile("/fileA.java", "package com.test; public class ClassA {}");
		final ProjectFile fileB = new ProjectFile("/fileB.java", "package com.test; public class ClassB {}");
		final ProjectFiles oldFiles = new ProjectFiles();
		oldFiles.insertFile(fileA);
		final ProjectFiles newFiles = new ProjectFiles();
		newFiles.insertFile(fileA);
		newFiles.insertFile(fileB);

		List<StriffDiagram> diagrams = new StriffOperation(oldFiles, newFiles,
				StriffConfig.create()
						.setOutputMode(OutputMode.DEFAULT)
						.setLanguages(List.of(Lang.JAVA)))
				.result().diagrams();
		assertFalse(diagrams.get(0).svg().contains("data-qualified-name=\"com-test-ClassA\""));
		assertTrue(diagrams.get(0).svg().contains("data-qualified-name=\"com-test-ClassB\""));
	}

	@Test
	public void testDiagramContainsNewClass() throws PUMLDrawException, IOException, CompileException {
		final ProjectFile fileA = new ProjectFile("/fileA.java", "public class ClassA {public String test;}");
		final ProjectFiles oldFiles = new ProjectFiles();
		final ProjectFiles newFiles = new ProjectFiles();
		newFiles.insertFile(fileA);

		List<StriffDiagram> diagrams = new StriffOperation(oldFiles, newFiles,
				StriffConfig.create()
						.setOutputMode(OutputMode.DEFAULT)
						.setLanguages(List.of(Lang.JAVA)))
				.result().diagrams();
		assertTrue(diagrams.get(0).svg().contains("<rect fill=\"#BEF5CB\""));
	}

	@Test
	public void testDiagramContainsDeletedClass() throws PUMLDrawException, IOException, CompileException {
		final ProjectFile fileA = new ProjectFile("/fileA.java", "public class ClassA {public String test;}");
		final ProjectFiles oldFiles = new ProjectFiles();
		oldFiles.insertFile(fileA);
		final ProjectFiles newFiles = new ProjectFiles();

		List<StriffDiagram> diagrams = new StriffOperation(oldFiles, newFiles,
				StriffConfig.create()
						.setOutputMode(OutputMode.DEFAULT)
						.setLanguages(List.of(Lang.JAVA)))
				.result().diagrams();

		assertTrue(diagrams.get(0).svg().contains("<rect fill=\"#FDAEB7\""));
	}

	@Test
	public void testDiagramForNestedClasses() throws PUMLDrawException, IOException, CompileException {
		final ProjectFile fileA = new ProjectFile("/fileA.java",
				"public class Outer { class Inner {} public static void main(String[] args) {} }");
		final ProjectFiles oldFiles = new ProjectFiles();
		final ProjectFiles newFiles = new ProjectFiles();
		oldFiles.insertFile(fileA);

		List<StriffDiagram> diagrams = new StriffOperation(oldFiles, newFiles,
				StriffConfig.create()
						.setOutputMode(OutputMode.DEFAULT)
						.setLanguages(List.of(Lang.JAVA)))
				.result().diagrams();
		assertTrue(diagrams.get(0).svg().contains("data-qualified-name=\"Outer\"")
				&& diagrams.get(0).svg().contains("data-qualified-name=\"Outer-Inner\""));
	}

	@Test
	public void testDiagramForMultipleNestedClassesWithSameName()
			throws PUMLDrawException, IOException, CompileException {
		final ProjectFile fileA = new ProjectFile("/fileA.java",
				"public class Outer { class Inner {} public static void main(String[] args) {} }");
		final ProjectFile fileB = new ProjectFile("/fileB.java",
				"public class Outer2 { class Inner {} public static void main(String[] args) {} }");
		final ProjectFiles oldFiles = new ProjectFiles();
		final ProjectFiles newFiles = new ProjectFiles();
		oldFiles.insertFile(fileA);
		newFiles.insertFile(fileB);

		List<StriffDiagram> diagrams = new StriffOperation(oldFiles, newFiles,
				StriffConfig.create()
						.setOutputMode(OutputMode.DEFAULT)
						.setLanguages(List.of(Lang.JAVA)))
				.result().diagrams();
		assertTrue(diagrams.get(0).svg().contains("data-qualified-name=\"Outer\"")
				&& diagrams.get(0).svg().contains("data-qualified-name=\"Outer2-Inner\"")
				&& diagrams.get(0).svg().contains("data-qualified-name=\"Outer2\"")
				&& diagrams.get(0).svg().contains("data-qualified-name=\"Outer-Inner\""));
	}
}
