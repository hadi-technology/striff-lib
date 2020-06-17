package com.hadii.stiff;

import com.hadii.stiff.diagram.DiagramComponent;
import com.hadii.stiff.diagram.StiffComponentPartitions;
import com.hadii.stiff.parse.DiffCodeModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Entry point for Stiff diagram generation.
 */
public class StiffOperation {

    private final int softMaxSizeLimit;
    private final List<String> sourceFilesFilter;
    private final int contextLevel;
    private List<Set<DiagramComponent>> diagramComponents = new ArrayList<>();
    private final List<StiffDiagram> stiffDiagrams = new ArrayList<>();

    public StiffOperation(DiffCodeModel diffedModel, int softMaxSizeLimit, List<String> sourceFilesFilter,
                          int contextLevel) throws Exception {
        int minDiagramSize = 4;
        if (softMaxSizeLimit < minDiagramSize) {
            throw new IllegalArgumentException("The requested max diagram size must be greater than " + minDiagramSize);
        }
        this.softMaxSizeLimit = softMaxSizeLimit;
        this.sourceFilesFilter = sourceFilesFilter;
        this.contextLevel = contextLevel;
        partitionDiagramComponents(diffedModel);
        genDiagrams(diffedModel);
    }

    public StiffOperation(DiffCodeModel diffedModel, int softMaxSizeLimit, List<String> sourceFilesFilter)
            throws Exception {
        this(diffedModel, softMaxSizeLimit, sourceFilesFilter, 2);
    }

    private void partitionDiagramComponents(DiffCodeModel diffedModel) throws NoStructuralChangesException {
        // Generate a list of all the BASE components we want to include in the final diagram(s)
        StiffCodeModel stiffCodeModel = null;
        if (this.sourceFilesFilter.isEmpty()) {
            stiffCodeModel = new StiffCodeModel(diffedModel);
        } else {
            stiffCodeModel = new StiffCodeModel(diffedModel, this.sourceFilesFilter);
        }
        List<Set<DiagramComponent>> diagramComponentSets = new ArrayList<>();
        if (stiffCodeModel.allComponents().size() < 1) {
            throw new NoStructuralChangesException("No structural changes were found between the given code bases!");
        } else if (stiffCodeModel.allComponents().size() > softMaxSizeLimit) {
            // Current diagram would be too large, lets partition the current base component set into
            // smaller sets to produce multiple, more readable diagrams
            List<Set<DiagramComponent>> componentPartitions = new StiffComponentPartitions(
                    stiffCodeModel, this.softMaxSizeLimit, this.contextLevel).partitions();
            diagramComponentSets.addAll(componentPartitions);
        } else {
            diagramComponentSets.add(stiffCodeModel.allComponents());
        }
        this.diagramComponents = diagramComponentSets;
    }

    private void genDiagrams(DiffCodeModel mergedModel) throws Exception {
        for (Set<DiagramComponent> diagramComponentSet : this.diagramComponents) {
            this.stiffDiagrams.add(new StiffDiagram(mergedModel, diagramComponentSet));
        }
    }

    public List<StiffDiagram> result() {
        return this.stiffDiagrams;
    }
}
