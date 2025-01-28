package striff.test;

import com.hadii.clarpse.compiler.CompileException;
import com.hadii.clarpse.compiler.Lang;
import com.hadii.clarpse.compiler.ProjectFile;
import com.hadii.clarpse.compiler.ProjectFiles;
import com.hadii.striff.StriffConfig;
import com.hadii.striff.StriffOperation;
import com.hadii.striff.diagram.StriffDiagram;
import com.hadii.striff.diagram.display.OutputMode;
import com.hadii.striff.diagram.plantuml.PUMLDrawException;
import com.hadii.striff.diagram.plantuml.PUMLHelper;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class PUMLTest {

    @Test
    public void testDetectPUMLError() throws PUMLDrawException, IOException {
        final String invalidPUMLString = " \n@startuml \n package { } } \n@enduml";
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
    public void testDiagramContainsIDsForClassComponents() throws PUMLDrawException, IOException, CompileException {
        final ProjectFile fileA = new ProjectFile("/fileA.java", "public class ClassA {}");
        final ProjectFile fileB = new ProjectFile("/fileB.java", "public class ClassB {}");
        final ProjectFiles oldFiles = new ProjectFiles();
        oldFiles.insertFile(fileB);
        final ProjectFiles newFiles = new ProjectFiles();
        newFiles.insertFile(fileA);
        newFiles.insertFile(fileB);

        List<StriffDiagram> diagrams = new StriffOperation(oldFiles, newFiles,
                StriffConfig.create()
                        .setOutputMode(OutputMode.DEFAULT)
                        .setLanguages(List.of(Lang.JAVA)))
                .result().diagrams();

        assertTrue(diagrams.get(0).svg().contains("<text id=\"ClassA\""));
    }

    @Test
    public void testDiagramContainsNewField() throws PUMLDrawException, IOException, CompileException {
        final ProjectFile fileA = new ProjectFile("/fileA.java", "public class ClassA {public String test;}");
        final ProjectFiles oldFiles = new ProjectFiles();
        final ProjectFiles newFiles = new ProjectFiles();
        newFiles.insertFile(fileA);

        List<StriffDiagram> diagrams = new StriffOperation(oldFiles, newFiles,
                StriffConfig.create()
                        .setOutputMode(OutputMode.DEFAULT)
                        .setLanguages(List.of(Lang.JAVA)))
                .result().diagrams();

        assertTrue(diagrams.get(0).svg().contains("<back:#bef5cb>test : String</back>"));
    }

    @Test
    public void testDiagramContainsDeletedField() throws PUMLDrawException, IOException, CompileException {
        final ProjectFile fileA = new ProjectFile("/fileA.java", "public class ClassA {public String test;}");
        final ProjectFiles oldFiles = new ProjectFiles();
        oldFiles.insertFile(fileA);
        final ProjectFiles newFiles = new ProjectFiles();

        List<StriffDiagram> diagrams = new StriffOperation(oldFiles, newFiles,
                StriffConfig.create()
                        .setOutputMode(OutputMode.DEFAULT)
                        .setLanguages(List.of(Lang.JAVA)))
                .result().diagrams();

        assertTrue(diagrams.get(0).svg().contains("<back:#fdaeb7>test : String</back>"));
    }

    @Test
    public void testDiagramContainsNewMethod() throws PUMLDrawException, IOException, CompileException {
        final ProjectFile fileA = new ProjectFile("/fileA.java", "public class ClassA {public String test() {}}");
        final ProjectFiles oldFiles = new ProjectFiles();
        final ProjectFiles newFiles = new ProjectFiles();
        newFiles.insertFile(fileA);

        List<StriffDiagram> diagrams = new StriffOperation(oldFiles, newFiles,
                StriffConfig.create()
                        .setOutputMode(OutputMode.DEFAULT)
                        .setLanguages(List.of(Lang.JAVA)))
                .result().diagrams();

        assertTrue(diagrams.get(0).svg().contains("<back:#bef5cb>test() : String</back>"));
    }

    @Test
    public void testDiagramContainsDeletedMethod() throws PUMLDrawException, IOException, CompileException {
        final ProjectFile fileA = new ProjectFile("/fileA.java", "public class ClassA {public String test() {}}");
        final ProjectFiles oldFiles = new ProjectFiles();
        final ProjectFiles newFiles = new ProjectFiles();
        oldFiles.insertFile(fileA);

        List<StriffDiagram> diagrams = new StriffOperation(oldFiles, newFiles,
                StriffConfig.create()
                        .setOutputMode(OutputMode.DEFAULT)
                        .setLanguages(List.of(Lang.JAVA)))
                .result().diagrams();

        assertTrue(diagrams.get(0).svg().contains("<back:#fdaeb7>test() : String</back>"));
    }
}
