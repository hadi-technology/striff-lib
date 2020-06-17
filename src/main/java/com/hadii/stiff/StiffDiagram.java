package com.hadii.stiff;

import com.hadii.stiff.diagram.DiagramComponent;
import com.hadii.stiff.diagram.plantuml.PUMLDiagram;
import com.hadii.stiff.diagram.scheme.LightDiagramColorScheme;
import com.hadii.stiff.parse.DiffCodeModel;

import java.util.Set;

/**
 * Represents a Striff diagram.
 */
public class StiffDiagram {

    private final String svgCode;
    private final Set<DiagramComponent> diagramComponents;

    public StiffDiagram(DiffCodeModel mergedModel, Set<DiagramComponent> diagramComponentSet) throws Exception {
        this.svgCode = new PUMLDiagram(mergedModel, new LightDiagramColorScheme(), diagramComponentSet).svgText();
        this.diagramComponents = diagramComponentSet;
    }

    public String svg() {
        return this.svgCode;
    }

    public int size() {
        return this.diagramComponents.size();
    }
}
