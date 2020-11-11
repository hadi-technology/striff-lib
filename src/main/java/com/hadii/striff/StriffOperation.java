package com.hadii.striff;

import com.hadii.striff.diagram.DiagramComponent;
import com.hadii.striff.diagram.StiffComponentPartitions;
import com.hadii.striff.diagram.plantuml.PUMLDrawException;
import com.hadii.striff.parse.DiffCodeModel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Entry point for Stiff diagram generation.
 */
public class StriffOperation {

    private final int softMaxSizeLimit;
    private final List<String> sourceFilesFilter;
    private final int contextLevel;
    private List<Set<DiagramComponent>> diagramComponents = new ArrayList<>();
    private final List<StriffDiagram> striffDiagrams = new ArrayList<>();

    public StriffOperation(DiffCodeModel diffedModel, int softMaxSizeLimit, List<String> sourceFilesFilter,
                           int contextLevel) throws NoStructuralChangesException, IOException, PUMLDrawException {
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

    public StriffOperation(DiffCodeModel diffedModel, int softMaxSizeLimit, List<String> sourceFilesFilter)
            throws Exception {
        this(diffedModel, softMaxSizeLimit, sourceFilesFilter, 2);
    }

    private void partitionDiagramComponents(DiffCodeModel diffedModel) throws NoStructuralChangesException {
        // Generate a list of all the BASE components we want to include in the final diagram(s)
        StriffCodeModel striffCodeModel = null;
        if (this.sourceFilesFilter.isEmpty()) {
            striffCodeModel = new StriffCodeModel(diffedModel);
        } else {
            striffCodeModel = new StriffCodeModel(diffedModel, this.sourceFilesFilter);
        }
        List<Set<DiagramComponent>> diagramComponentSets = new ArrayList<>();
        if (striffCodeModel.allComponents().size() < 1) {
            throw new NoStructuralChangesException("No structural changes were found between the given code bases!");
        } else if (striffCodeModel.allComponents().size() > softMaxSizeLimit) {
            // Current diagram would be too large, lets partition the current base component set into
            // smaller sets to produce multiple, more readable diagrams
            List<Set<DiagramComponent>> componentPartitions = new StiffComponentPartitions(
                    striffCodeModel, this.softMaxSizeLimit, this.contextLevel).partitions();
            diagramComponentSets.addAll(componentPartitions);
        } else {
            diagramComponentSets.add(striffCodeModel.allComponents());
        }
        this.diagramComponents = diagramComponentSets;
    }

    private void genDiagrams(DiffCodeModel mergedModel) throws IOException, PUMLDrawException {
        for (Set<DiagramComponent> diagramComponentSet : this.diagramComponents) {
            this.striffDiagrams.add(new StriffDiagram(mergedModel, diagramComponentSet));
        }
    }

    public List<StriffDiagram> result() {
        return this.striffDiagrams;
    }
}
