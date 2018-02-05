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
        OOPSourceCodeModel oldModel = ClarityTestUtil.getGitHubRepoModel("exercism",
                "go", "887a32f66b666b5f15fe9122319a2931fbd83185", "01fe37630d630dfecbd504faed22688d6de0aab6", Lang.GOLANG);
        OOPSourceCodeModel newModel = ClarityTestUtil.getGitHubRepoModel(
                "ferhatelmas", "go", "rectangles", "01fe37630d630dfecbd504faed22688d6de0aab6", Lang.GOLANG);
        Diagram view = new SDView(new LightDiagramColorScheme(), oldModel, newModel, 400).view();
        PrintWriter writer = new PrintWriter("/home/zir0/Desktop/sdTest.svg", "UTF-8");
        writer.println(view.svgText());
        writer.close();
    }


    @Test
    public void emptyStructureDiffTest() throws Exception {
        PUMLDiagram pumlDiagram = new PUMLDiagram(new EmptyStructureDiffDiagramDesciption(), new LightDiagramColorScheme(), 0);
        assertTrue(pumlDiagram.svgText().isEmpty());
    }
}
