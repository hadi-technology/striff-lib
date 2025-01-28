package com.hadii.striff.diagram.plantuml;

import com.hadii.striff.diagram.display.DiagramDisplay;
import com.hadii.striff.extractor.RelationsMap;
import com.hadii.clarpse.sourcemodel.OOPSourceCodeModel;
import com.hadii.striff.diagram.DiagramComponent;
import java.util.Set;

public class PUMLDiagramData {
    private final RelationsMap diagramRels;
    private final RelationsMap addedRels;
    private final RelationsMap deletedRels;
    private final DiagramDisplay diagramDisplay;
    private final OOPSourceCodeModel mergedModel;
    private final Set<String> addedCmps;
    private final Set<String> deletedCmps;
    private final Set<String> modifiedCmps;
    private final Set<DiagramComponent> diagramCmps;

    public PUMLDiagramData(RelationsMap diagramRels, RelationsMap addedRels, RelationsMap deletedRels,
            DiagramDisplay diagramDisplay, OOPSourceCodeModel mergedModel, Set<String> addedCmps,
            Set<String> deletedCmps, Set<String> modifiedCmps, Set<DiagramComponent> diagramCmps) {
        this.diagramRels = diagramRels;
        this.addedRels = addedRels;
        this.deletedRels = deletedRels;
        this.diagramDisplay = diagramDisplay;
        this.mergedModel = mergedModel;
        this.addedCmps = addedCmps;
        this.deletedCmps = deletedCmps;
        this.modifiedCmps = modifiedCmps;
        this.diagramCmps = diagramCmps;
    }

    public RelationsMap diagramRels() {
        return diagramRels;
    }

    public RelationsMap addedRels() {
        return addedRels;
    }

    public RelationsMap deletedRels() {
        return deletedRels;
    }

    public DiagramDisplay diagramDisplay() {
        return diagramDisplay;
    }

    public OOPSourceCodeModel mergedModel() {
        return mergedModel;
    }

    public Set<String> addedCmps() {
        return addedCmps;
    }

    public Set<String> deletedCmps() {
        return deletedCmps;
    }

    public Set<String> modifiedCmps() {
        return modifiedCmps;
    }

    public Set<DiagramComponent> diagramCmps() {
        return diagramCmps;
    }
}
