package claritybot.test;

import com.clarity.binary.diagram.Diagram;
import com.clarity.binary.diagram.DiagramSourceCodeModel;
import com.clarity.binary.diagram.scheme.LightDiagramColorScheme;
import com.clarity.binary.diagram.view.EmptySDException;
import com.clarity.binary.diagram.view.SDView;
import com.clarity.compiler.Lang;
import com.clarity.sourcemodel.OOPSourceCodeModel;
import org.junit.Test;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class SDViewTest {

    @Test
    public void manualTest() throws Exception {
        String baseRepoOwner = "canoo";
        String repoName = "dolphin-platform";
        String prNumber = "900";
        String token = "cb9d67bb5599db05ad0a387d3b3051e1f9900b9b";
        Lang language = Lang.JAVA;

        List<String> changedFiles = ClarityTestUtil.pullRequestChangedFiles(baseRepoOwner, repoName, token, prNumber);

        DiagramSourceCodeModel oldModel = ClarityTestUtil.getGitHubRepoModel(baseRepoOwner,
                repoName, "5fd5b9c6ad9aa31d22dd9923758eb53ddd71c484", token, language);

        DiagramSourceCodeModel newModel = ClarityTestUtil.getGitHubRepoModel(
                "canoo", repoName, "f0df41407c6cf504c0a77d86439801559ef47203", token, language);


        Diagram view = new SDView(new LightDiagramColorScheme(), oldModel, newModel, 400, changedFiles).view();
        PrintWriter writer = new PrintWriter("/home/zir0/Desktop/sdTest.svg", "UTF-8");
        writer.println(view.svgText());
        writer.close();
    }

    @Test(expected = EmptySDException.class)
    public void testEmptySDException() throws Exception {
        List<String> changedFiles = new ArrayList<>();
        DiagramSourceCodeModel oldModel = new DiagramSourceCodeModel(new OOPSourceCodeModel());
        DiagramSourceCodeModel newModel = new DiagramSourceCodeModel(new OOPSourceCodeModel());
        new SDView(new LightDiagramColorScheme(), oldModel, newModel, 400, changedFiles).view();
    }
}
