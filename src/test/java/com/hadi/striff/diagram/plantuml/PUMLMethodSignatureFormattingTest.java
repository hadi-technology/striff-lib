package com.hadi.striff.diagram.plantuml;

import com.hadi.clarpse.sourcemodel.Component;
import com.hadi.clarpse.sourcemodel.OOPSourceCodeModel;
import com.hadi.clarpse.sourcemodel.OOPSourceModelConstants;
import com.hadi.striff.diagram.DiagramComponent;
import com.hadi.striff.diagram.display.DiagramDisplay;
import com.hadi.striff.diagram.display.LightDiagramColorScheme;
import com.hadi.striff.extractor.RelationsMap;
import org.junit.Test;

import java.util.Set;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class PUMLMethodSignatureFormattingTest {

    @Test
    public void truncatesLongMethodSignatureAndPreservesReturnType() {
        OOPSourceCodeModel model = new OOPSourceCodeModel();

        Component parent = new Component();
        parent.setComponentType(OOPSourceModelConstants.ComponentType.CLASS);
        parent.setComponentName("ExampleService");
        parent.setValue("ExampleService");
        model.insertComponent(parent);

        Component method = new Component();
        method.setComponentType(OOPSourceModelConstants.ComponentType.METHOD);
        method.setComponentName("ExampleService.loadEverything");
        method.setValue("ExampleService.loadEverything");
        method.setCodeFragment(
                "loadEverything(firstParameter: String, secondParameter: Integer, thirdParameter: VeryLongTypeName, fourthParameter: Map<String, Object>) : ResponseEntity<ResultPayload>");
        method.insertAccessModifier("public");
        model.insertComponent(method);
        parent.insertChildComponent(method.uniqueName());

        DiagramComponent parentDiagram = new DiagramComponent(parent, model);
        DiagramDisplay display = new DiagramDisplay(new LightDiagramColorScheme(), Set.of(""));
        PUMLDiagramData data = new PUMLDiagramData(
                new RelationsMap(),
                new RelationsMap(),
                new RelationsMap(),
                display,
                model,
                Set.of(),
                Set.of(),
                Set.of(),
                Set.of(parentDiagram));

        String puml = new PUMLClassFieldsCode(data).value(Set.of(parentDiagram));

        assertTrue(puml.contains("loadEverything(..."));
        assertTrue(puml.contains("...) : ResponseEntity<ResultPayload>"));
        assertFalse(puml.contains("firstParameter: String"));
    }

    @Test
    public void leavesShortMethodSignatureUnchanged() {
        OOPSourceCodeModel model = new OOPSourceCodeModel();

        Component parent = new Component();
        parent.setComponentType(OOPSourceModelConstants.ComponentType.CLASS);
        parent.setComponentName("ExampleService");
        parent.setValue("ExampleService");
        model.insertComponent(parent);

        Component method = new Component();
        method.setComponentType(OOPSourceModelConstants.ComponentType.METHOD);
        method.setComponentName("ExampleService.ping");
        method.setValue("ExampleService.ping");
        method.setCodeFragment("ping() : String");
        method.insertAccessModifier("public");
        model.insertComponent(method);
        parent.insertChildComponent(method.uniqueName());

        DiagramComponent parentDiagram = new DiagramComponent(parent, model);
        DiagramDisplay display = new DiagramDisplay(new LightDiagramColorScheme(), Set.of(""));
        PUMLDiagramData data = new PUMLDiagramData(
                new RelationsMap(),
                new RelationsMap(),
                new RelationsMap(),
                display,
                model,
                Set.of(),
                Set.of(),
                Set.of(),
                Set.of(parentDiagram));

        String puml = new PUMLClassFieldsCode(data).value(Set.of(parentDiagram));

        assertTrue(puml.contains("ping() : String"));
        assertFalse(puml.contains("ping(...)"));
    }
}
