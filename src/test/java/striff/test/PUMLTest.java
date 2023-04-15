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
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.List;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertFalse;

public class PUMLTest {

    @Test
    public void testDetectPUMLErrorTest() throws PUMLDrawException, IOException {
        final String invalidPUMLString = " \n@startuml \n package { } } \n@enduml";
        final byte[] diagram = PUMLHelper.generateDiagram(invalidPUMLString);
        final String diagramStr = new String(diagram);
        assertTrue(PUMLHelper.invalidPUMLDiagram(diagramStr));
    }

    @Test
    public void testNoPumlErrorDetected() throws PUMLDrawException, IOException {
        final String invalidPUMLString = " \n@startuml \n package {} \n@enduml";
        final byte[] diagram = PUMLHelper.generateDiagram(invalidPUMLString);
        final String diagramStr = new String(diagram);
        assertFalse(PUMLHelper.invalidPUMLDiagram(diagramStr));
    }

    @Test
    public void testDiagramContainsIDsForClassCmps() throws PUMLDrawException, IOException, CompileException {
        final ProjectFile fileA = new ProjectFile("/fileA.java", "public class ClassA {}");
        final ProjectFile fileB = new ProjectFile("/fileB.java", "public class ClassB {}");
        final ProjectFiles oldFiles = new ProjectFiles();
        oldFiles.insertFile(fileB);
        final ProjectFiles newFiles = new ProjectFiles();
        newFiles.insertFile(fileA);
        newFiles.insertFile(fileB);
        List<StriffDiagram> diagrams = new StriffOperation(
            oldFiles, newFiles,
            new StriffConfig(OutputMode.DEFAULT, List.of(Lang.JAVA))).result().diagrams();
        // Empty files filter means changes across all files are included in generated striffs.
        Assert.assertTrue(diagrams.get(0).svg().contains("<text id=\"ClassA\""));
    }
}