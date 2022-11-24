package striff.test;

import com.hadii.striff.diagram.plantuml.PUMLDrawException;
import com.hadii.striff.diagram.plantuml.PUMLHelper;
import org.junit.Test;

import java.io.IOException;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertFalse;

public class PUMLTest {

    @Test
    public void detectPUMLErrorTest() throws PUMLDrawException, IOException {
        final String invalidPUMLString = " \n@startuml \n package { } } \n@enduml";
        final byte[] diagram = PUMLHelper.generateDiagram(invalidPUMLString);
        final String diagramStr = new String(diagram);
        assertTrue(PUMLHelper.invalidPUMLDiagram(diagramStr));
    }

    @Test
    public void noPumlErrorDetected() throws PUMLDrawException, IOException {
        final String invalidPUMLString = " \n@startuml \n package {} \n@enduml";
        final byte[] diagram = PUMLHelper.generateDiagram(invalidPUMLString);
        final String diagramStr = new String(diagram);
        assertFalse(PUMLHelper.invalidPUMLDiagram(diagramStr));
    }
}