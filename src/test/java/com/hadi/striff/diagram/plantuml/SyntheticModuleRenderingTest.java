package com.hadi.striff.diagram.plantuml;

import com.hadi.clarpse.sourcemodel.Component;
import com.hadi.clarpse.sourcemodel.OOPSourceCodeModel;
import com.hadi.clarpse.sourcemodel.OOPSourceModelConstants;
import com.hadi.striff.diagram.DiagramComponent;
import com.hadi.striff.diagram.SyntheticModuleSupport;
import com.hadi.striff.diagram.display.DiagramDisplayOverride;
import com.hadi.striff.diagram.display.DiagramDisplay;
import com.hadi.striff.diagram.display.DiagramColorSchemeOverride;
import com.hadi.striff.diagram.display.LightDiagramColorScheme;
import com.hadi.striff.extractor.RelationsMap;
import org.junit.Test;

import java.util.Set;

import static org.junit.Assert.assertTrue;

public class SyntheticModuleRenderingTest {

    @Test
    public void rendersSyntheticLabelAndModuleFunction() {
        OOPSourceCodeModel model = new OOPSourceCodeModel();

        Component fn = new Component();
        fn.setComponentName("topLevelFn");
        fn.setComponentType(OOPSourceModelConstants.ComponentType.FUNCTION);
        fn.setModule("util");
        fn.setCodeFragment("doThing()");
        model.insertComponent(fn);

        Component synthetic = SyntheticModuleSupport.syntheticComponent("util", Set.of(fn.uniqueName()));
        DiagramComponent syntheticDiagram = new DiagramComponent(synthetic, null);
        syntheticDiagram.putAugmentation("synthetic", true);
        syntheticDiagram.putAugmentation("syntheticDisplayName", "util");

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
                Set.of(syntheticDiagram));

        String puml = new PUMLClassFieldsCode(data).value(Set.of(syntheticDiagram));
        // Module circle with lighter dark red, plain synthetic text (no color codes in text)
        assertTrue(puml.contains("<< (M,#B22222)>><<synthetic>>"));
        assertTrue(puml.contains("synthetic"));
        assertTrue(puml.contains("doThing()"));
    }

    @Test
    public void stripsModulePrefixFromDisplayName() {
        OOPSourceCodeModel model = new OOPSourceCodeModel();

        Component component = new Component();
        component.setComponentName("SessionManager.SessionManager");
        component.setModule("SessionManager");
        component.setComponentType(OOPSourceModelConstants.ComponentType.CLASS);
        model.insertComponent(component);

        DiagramComponent diagramComponent = new DiagramComponent(component, model);
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
                Set.of(diagramComponent));

        String puml = new PUMLClassFieldsCode(data).value(Set.of(diagramComponent));
        assertTrue(!puml.contains("SessionManager.SessionManager"));
        assertTrue(puml.contains("SessionManager\""));
    }

    @Test
    public void moduleQualifiedNameUsesPumlIdInSvg() throws Exception {
        OOPSourceCodeModel model = new OOPSourceCodeModel();

        Component fn = new Component();
        fn.setComponentName("topLevelFn");
        fn.setComponentType(OOPSourceModelConstants.ComponentType.FUNCTION);
        fn.setModule("src.main");
        fn.setCodeFragment("doThing()");
        model.insertComponent(fn);

        Component synthetic = SyntheticModuleSupport.syntheticComponent("src.main", Set.of(fn.uniqueName()));
        DiagramComponent syntheticDiagram = new DiagramComponent(synthetic, model);
        syntheticDiagram.putAugmentation("synthetic", true);
        syntheticDiagram.putAugmentation("syntheticDisplayName", "src.main");

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
                Set.of(syntheticDiagram));

        String svg = new PUMLDiagram(data).svgText();
        java.util.regex.Matcher matcher = java.util.regex.Pattern
                .compile("data-qualified-name=\\\"([^\\\"]+)\\\"")
                .matcher(svg);
        java.util.Set<String> qualifiedNames = new java.util.HashSet<>();
        while (matcher.find()) {
            qualifiedNames.add(matcher.group(1));
        }
        assertTrue("Qualified names: " + qualifiedNames,
                qualifiedNames.contains("module:src.main"));
    }

    @Test
    public void syntheticLabelColorPropagatesToSvg() throws Exception {
        OOPSourceCodeModel model = new OOPSourceCodeModel();

        Component fn = new Component();
        fn.setComponentName("topLevelFn");
        fn.setComponentType(OOPSourceModelConstants.ComponentType.FUNCTION);
        fn.setModule("util");
        fn.setCodeFragment("doThing()");
        model.insertComponent(fn);

        Component synthetic = SyntheticModuleSupport.syntheticComponent("util", Set.of(fn.uniqueName()));
        DiagramComponent syntheticDiagram = new DiagramComponent(synthetic, model);
        syntheticDiagram.putAugmentation("synthetic", true);
        syntheticDiagram.putAugmentation("syntheticDisplayName", "util");

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
                Set.of(syntheticDiagram));

        String svg = new PUMLDiagram(data).svgText().toLowerCase();
        // Debug: print the SVG to see what we get
        System.out.println("=== SVG OUTPUT ===");
        System.out.println(svg);
        System.out.println("=== END SVG OUTPUT ===");
        assertTrue(svg.contains("synthetic"));
        // Check for lighter dark red module circle (#B22222 firebrick)
        assertTrue(svg.contains("#b22222"));
        // The stereotype text should be plain «synthetic» (HTML encoded as &#171;synthetic&#187;)
        // NOT the colored version with #color codes
        assertTrue(svg.contains("&#171;synthetic&#187;"));
    }

    @Test
    public void rootSyntheticPackageUsesConfiguredRootPackageColor() throws Exception {
        OOPSourceCodeModel model = new OOPSourceCodeModel();

        Component fn = new Component();
        fn.setComponentName("topLevelFn");
        fn.setComponentType(OOPSourceModelConstants.ComponentType.FUNCTION);
        fn.setModule("util");
        fn.setCodeFragment("doThing()");
        model.insertComponent(fn);

        Component synthetic = SyntheticModuleSupport.syntheticComponent("util", Set.of(fn.uniqueName()));
        DiagramComponent syntheticDiagram = new DiagramComponent(synthetic, model);
        syntheticDiagram.putAugmentation("synthetic", true);
        syntheticDiagram.putAugmentation("syntheticDisplayName", "util");

        DiagramDisplay display = new DiagramDisplay(new LightDiagramColorScheme(), Set.of(""))
                .withPackageColors(java.util.Map.of("", "#ABCDEF66"))
                .merge(new DiagramDisplayOverride()
                        .setPackageBorderColor("#24292E")
                        .setPackageBorderThickness("1"));
        PUMLDiagramData data = new PUMLDiagramData(
                new RelationsMap(),
                new RelationsMap(),
                new RelationsMap(),
                display,
                model,
                Set.of(),
                Set.of(),
                Set.of(),
                Set.of(syntheticDiagram));

        String svg = new PUMLDiagram(data).svgText();
        assertTrue(svg.toLowerCase().contains("fill=\"#abcdef\""));
        assertTrue(svg.contains("data-qualified-name=\"module:util\""));
    }
}
