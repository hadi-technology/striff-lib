package claritybot.test;

import com.clarity.binary.diagram.Diagram;
import com.clarity.binary.diagram.plantuml.EmptyStructureDiffDiagramDesciption;
import com.clarity.binary.diagram.plantuml.PUMLDiagram;
import com.clarity.binary.diagram.scheme.LightDiagramColorScheme;
import com.clarity.binary.diagram.view.SDView;
import com.clarity.compiler.Lang;
import com.clarity.sourcemodel.OOPSourceCodeModel;
import org.junit.Test;

import java.io.PrintWriter;

import static org.junit.Assert.assertTrue;

public class SDManualTest {

    @Test
    public void structureDiffManualTest() throws Exception {
        OOPSourceCodeModel oldModel = ClarityTestUtil.getGitHubRepoModel("junit-team",
                "junit5", "2584afa91ac53224019cb341a61a760f2110a5b0", "d812a6ef9729a381a374907a96bfe74bdf60173e", Lang.JAVA);
        OOPSourceCodeModel newModel = ClarityTestUtil.getGitHubRepoModel(
                "lutovich", "junit5", "e3a4e4e621664e661c701a972fee06d0c8d7511c", "d812a6ef9729a381a374907a96bfe74bdf60173e", Lang.JAVA);
        Diagram view = new SDView(new LightDiagramColorScheme(), oldModel, newModel, 400).view();
        PrintWriter writer = new PrintWriter("/home/zir0/Desktop/sdTest.svg", "UTF-8");
        writer.println(view.svgText());
        writer.close();
    }


    @Test
    public void emptyStructureDiffTest() {
        PUMLDiagram pumlDiagram = new PUMLDiagram(new EmptyStructureDiffDiagramDesciption(), new LightDiagramColorScheme(), 0);
        assertTrue(pumlDiagram.svgText().isEmpty());
    }
}
