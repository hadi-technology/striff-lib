package com.hadii.striff;

import com.hadii.striff.diagram.DiagramComponent;
import com.hadii.striff.diagram.DiagramSourceCodeModel;
import com.hadii.striff.diagram.partition.StriffComponentPartitions;
import com.hadii.striff.diagram.plantuml.PUMLDiagram;
import com.hadii.striff.diagram.scheme.DiagramColorScheme;
import com.hadii.striff.extractor.ComponentRelations;
import com.hadii.striff.parse.MergedSourceCodeModel;
import edu.emory.mathcs.backport.java.util.Collections;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Represents a Striff diagram demonstrating the structural differences between
 * the two given code bases.
 */
public class StriffDiagram {

    private final DiagramColorScheme colorScheme;
    private List<Set<DiagramComponent>> diagramComponents = new ArrayList<>();
    private final List<String> SVGDiagrams = new ArrayList<>();
    private ChangeSet changeSet;
    private MergedSourceCodeModel mergedModel;
    private ComponentRelations allRelations;

    public StriffDiagram(DiagramColorScheme colorScheme, DiagramSourceCodeModel olderModel, DiagramSourceCodeModel newerModel,
                         int softMaxSizeLimit) throws Exception {
        int MIN_DIAGRAM_SIZE = 4;
        if (softMaxSizeLimit < MIN_DIAGRAM_SIZE) {
            throw new IllegalArgumentException("The requested max diagram size must be greater than " + MIN_DIAGRAM_SIZE);
        }
        this.colorScheme = colorScheme;
        generateDiagrams(olderModel, newerModel, softMaxSizeLimit);
        drawDiagrams();
    }

    private void generateDiagrams(DiagramSourceCodeModel olderModel, DiagramSourceCodeModel newerModel, int softMaxSizeLimit) {
        // Calculate all the differences between the original and transformed code base.
        this.changeSet = new ChangeSet(olderModel, newerModel);
        this.mergedModel = new MergedSourceCodeModel(olderModel, newerModel);
        // Calculate all the UML relationships between all the components in the merged code base.
        this.allRelations = new ComponentRelations(this.mergedModel.model());
        // Generate a list of all the BASE components we want to include in the final diagram(s)
        SelectedStriffComponents diagramComponents = new SelectedStriffComponents(this.allRelations, changeSet);
        List<Set<DiagramComponent>> diagramComponentSets =  new ArrayList<>();
        if (diagramComponents.allComponents().size() < 1) {
            System.out.println("No major structural differences found!");
            return;
        } else if (diagramComponents.allComponents().size() > softMaxSizeLimit) {
            // Current diagram would be too large, lets partition the current base component set into
            // smaller sets to produce more readable diagrams
            Map<Integer, Set<DiagramComponent>> componentPartitions = new StriffComponentPartitions(
                    diagramComponents.allComponents(), this.allRelations
            ).partitions();
            // If a partition has no core components, it's not important, delete it.
            componentPartitions.forEach((label, partition) -> Collections.disjoint(partition, diagramComponents.coreComponents()));
            diagramComponentSets.addAll(componentPartitions.values());
        } else {
            diagramComponentSets.add(diagramComponents.allComponents());
        }
        this.diagramComponents = diagramComponentSets;
    }

    private void drawDiagrams() throws Exception {
        for (Set<DiagramComponent> diagramComponentSet : this.diagramComponents) {
            // Generate PlantUML diagram description and draw.
            this.SVGDiagrams.add(new PUMLDiagram(changeSet, colorScheme, diagramComponentSet,
                    this.mergedModel.model().components(), this.allRelations).svgText());
        }
    }

    /**
     * List of diagrams in SVG code format.
     */
    public List<String> svg() {
        return this.SVGDiagrams;
    }
}
