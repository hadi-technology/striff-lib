package com.hadii.striff;

import com.hadii.striff.diagram.DiagramComponent;
import com.hadii.striff.diagram.plantuml.PUMLDiagram;
import com.hadii.striff.diagram.plantuml.PUMLDrawException;
import com.hadii.striff.diagram.scheme.LightDiagramColorScheme;
import com.hadii.striff.parse.DiffCodeModel;

import java.io.IOException;
import java.util.Set;

/**
 * Represents a Striff diagram.
 */
public class StiffDiagram {

    private final String svgCode;
    private final Set<DiagramComponent> diagramComponents;

    public StiffDiagram(DiffCodeModel mergedModel, Set<DiagramComponent> diagramComponentSet) throws IOException, PUMLDrawException {
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
