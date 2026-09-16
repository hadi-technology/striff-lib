package striff.test.model;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hadi.clarpse.sourcemodel.Component;
import com.hadi.clarpse.sourcemodel.OOPSourceCodeModel;
import com.hadi.clarpse.sourcemodel.OOPSourceModelConstants;
import com.hadi.clarpse.sourcemodel.Package;
import com.hadi.striff.diagram.DiagramComponent;
import com.hadi.striff.diagram.SyntheticModuleSupport;
import org.junit.Test;

import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
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

    @Test
    public void serializesChildrenOfAClass() throws Exception {
        OOPSourceCodeModel model = new OOPSourceCodeModel();
        Component clazz = new Component();
        clazz.setComponentName("Zoo");
        clazz.setComponentType(OOPSourceModelConstants.ComponentType.CLASS);
        clazz.insertChildComponent("Zoo.name");
        clazz.insertChildComponent("Zoo.feed");
        model.insertComponent(clazz);
        model.insertComponent(childComponent("Zoo.name", OOPSourceModelConstants.ComponentType.FIELD));
        model.insertComponent(childComponent("Zoo.feed", OOPSourceModelConstants.ComponentType.METHOD));

        String json = new ObjectMapper().writeValueAsString(new DiagramComponent(clazz, model));

        assertTrue("children should be serialized: " + json, json.contains("\"children\""));
        assertTrue(json.contains("Zoo.name"));
        assertTrue(json.contains("Zoo.feed"));
    }

    /**
     * A synthetic module's members exist nowhere but its child list, so losing them in
     * serialization leaves the module looking empty to any consumer reading it back.
     */
    @Test
    public void syntheticModuleChildrenSurviveAJsonRoundTrip() throws Exception {
        OOPSourceCodeModel model = new OOPSourceCodeModel();
        Component readConfig = moduleLevelComponent(
                "readConfig", OOPSourceModelConstants.ComponentType.FUNCTION);
        Component writeConfig = moduleLevelComponent(
                "writeConfig", OOPSourceModelConstants.ComponentType.FUNCTION);
        Component defaultTimeout = moduleLevelComponent(
                "DEFAULT_TIMEOUT", OOPSourceModelConstants.ComponentType.MODULE_FIELD);
        model.insertComponent(readConfig);
        model.insertComponent(writeConfig);
        model.insertComponent(defaultTimeout);

        Component synthetic = SyntheticModuleSupport.syntheticComponent("config",
                List.of(readConfig.uniqueName(), writeConfig.uniqueName(), defaultTimeout.uniqueName()));
        DiagramComponent syntheticDiagramCmp = new DiagramComponent(synthetic, model);
        assertEquals(3, syntheticDiagramCmp.children().size());

        ObjectMapper mapper = new ObjectMapper()
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        String json = mapper.writeValueAsString(syntheticDiagramCmp);
        DiagramComponent readBack = mapper.readValue(json, DiagramComponent.class);

        assertEquals("All members should survive the round trip",
                Set.copyOf(syntheticDiagramCmp.children()), Set.copyOf(readBack.children()));
        assertTrue(readBack.children().contains(readConfig.uniqueName()));
        assertTrue(readBack.children().contains(writeConfig.uniqueName()));
        assertTrue(readBack.children().contains(defaultTimeout.uniqueName()));
    }

    private static Component childComponent(String name, OOPSourceModelConstants.ComponentType type) {
        Component child = new Component();
        child.setComponentName(name);
        child.setComponentType(type);
        return child;
    }

    private static Component moduleLevelComponent(String name, OOPSourceModelConstants.ComponentType type) {
        Component component = new Component();
        component.setComponentName(name);
        component.setComponentType(type);
        component.setModule("config");
        return component;
    }
}
