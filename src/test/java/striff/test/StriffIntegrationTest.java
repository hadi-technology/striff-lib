package striff.test;

import com.hadii.clarpse.compiler.Lang;
import com.hadii.clarpse.sourcemodel.OOPSourceCodeModel;
import com.hadii.striff.StriffDiagram;
import com.hadii.striff.diagram.DiagramSourceCodeModel;
import com.hadii.striff.diagram.scheme.LightDiagramColorScheme;
import org.junit.Test;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class StriffIntegrationTest {

    @Test
    public void manualTest() throws Exception {
        String baseRepoOwner = "hadii-tech";
        String repoName = "clarityviews-binary";
        String prNumber = "28";
        String token = "cb9d67bb5599db05ad0a387d3b3051e1f9900b9b";
        Lang language = Lang.JAVA;
        DiagramSourceCodeModel oldModel = ClarityTestUtil.getGitHubRepoModel(baseRepoOwner,
                repoName, "master", token, language);
        DiagramSourceCodeModel newModel = ClarityTestUtil.getGitHubRepoModel(
                "hadii-tech", repoName, "diagram_improvements", token, language);
        List<String> diagramSVGs = new StriffDiagram(new LightDiagramColorScheme(), oldModel, newModel, 10).svg();
        PrintWriter writer = new PrintWriter("/home/zir0/Desktop/sdTest.svg", "UTF-8");
        for (String diagramSVG : diagramSVGs) {
            writer.println(diagramSVG);
        }
        writer.close();
    }

    @Test(expected = Exception.class)
    public void testEmptySDException() throws Exception {
        List<String> changedFiles = new ArrayList<>();
        DiagramSourceCodeModel oldModel = new DiagramSourceCodeModel(new OOPSourceCodeModel());
        DiagramSourceCodeModel newModel = new DiagramSourceCodeModel(new OOPSourceCodeModel());
        new StriffDiagram(new LightDiagramColorScheme(), oldModel, newModel, 6);
    }
}
