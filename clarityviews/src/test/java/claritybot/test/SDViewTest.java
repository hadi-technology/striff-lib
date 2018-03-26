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
        String baseRepoOwner = "junit-team";
        String repoName = "junit5";
        String prNumber = "1340";
        String token = "01fe37630d630dfecbd504faed22688d6de0aab6";
        Lang language = Lang.JAVA;

        List<String> changedFiles = ClarityTestUtil.pullRequestChangedFiles(baseRepoOwner, repoName, token, prNumber);

        DiagramSourceCodeModel oldModel = ClarityTestUtil.getGitHubRepoModel(baseRepoOwner,
                repoName, "42443aa043fea087356eb5d3040523efe917b4f1", token, language);

        DiagramSourceCodeModel newModel = ClarityTestUtil.getGitHubRepoModel(
                "karollewandowski", repoName, "8af4b5c34a5fc1f8f58fecc3e45716d778e62df4", token, language);


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
