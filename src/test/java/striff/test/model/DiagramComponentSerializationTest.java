package striff.test.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hadi.clarpse.sourcemodel.Component;
import com.hadi.clarpse.sourcemodel.Package;
import com.hadi.striff.diagram.DiagramComponent;
import org.junit.Test;

import java.util.Set;

import static org.junit.Assert.assertTrue;

public class DiagramComponentSerializationTest {

    @Test
    public void serializesNullPackageAsEmptyString() throws Exception {
        Component component = new Component();
        component.setComponentName("NoPkg");
        DiagramComponent diagramComponent = new DiagramComponent(component, null);

        String json = new ObjectMapper().writeValueAsString(Set.of(diagramComponent));
        assertTrue(json.contains("\"package\":\"\""));
    }

    @Test
    public void serializesPackageWithoutPathSeparatorSuffix() throws Exception {
        Component component = new Component();
        component.setComponentName("ToolDefinition");
        component.setPkg(new Package("src/tools", "src/tools"));
        DiagramComponent diagramComponent = new DiagramComponent(component, null);

        String json = new ObjectMapper().writeValueAsString(Set.of(diagramComponent));
        assertTrue(json.contains("\"package\":\"src.tools\""));
    }
}
