package claritybot.test;

import com.clarity.binary.diagram.Diagram;
import com.clarity.binary.diagram.SignedSDDiagram;
import com.clarity.binary.diagram.scheme.LightDiagramColorScheme;
import com.clarity.binary.diagram.view.SDView;
import com.clarity.sourcemodel.OOPSourceCodeModel;
import org.junit.Test;

public class SDManualTest {

    @Test
    public void structureDiffManualTest() throws Exception {
        OOPSourceCodeModel oldModel = ClarityTestUtil.getGitHubRepoModel("structurizr", "java", "master","d812a6ef9729a381a374907a96bfe74bdf60173e");
        OOPSourceCodeModel newModel = ClarityTestUtil.getGitHubRepoModel("roxspring", "java", "feature/propogate-supporting-description","d812a6ef9729a381a374907a96bfe74bdf60173e");
        Diagram view = new SignedSDDiagram(new SDView(new LightDiagramColorScheme(), oldModel, newModel, 400).view(), "https://clarity-bot.com");
        System.out.println(view.svgText());
    }
}
