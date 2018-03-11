package com.clarity.binary.diagram.view;

import com.clarity.binary.MergedSourceCodeModel;
import com.clarity.binary.diagram.Diagram;
import com.clarity.binary.diagram.DiagramComponent;
import com.clarity.binary.diagram.DiagramConstants.BinaryClassAssociation;
import com.clarity.binary.diagram.DiagramSourceCodeModel;
import com.clarity.binary.diagram.RelatedBaseComponentsGroup;
import com.clarity.binary.diagram.plantuml.PUMLDiagram;
import com.clarity.binary.diagram.plantuml.PUMLDiagramDescription;
import com.clarity.binary.diagram.plantuml.StructureDiffPUMLDiagramDesciption;
import com.clarity.binary.diagram.scheme.DiagramColorScheme;
import com.clarity.binary.extractor.BinaryClassRelationship;
import com.clarity.binary.extractor.BinaryClassRelationshipExtractor;
import com.clarity.sourcemodel.OOPSourceModelConstants;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Represents a Structure-Diff demonstrating the structural differences between
 * the two given code bases.
 */
public class SDView implements ClarityBotView, Serializable {

    private static final long serialVersionUID = -3125810981280395679L;
    private Diagram diagram;

    public SDView(DiagramColorScheme colorScheme, DiagramSourceCodeModel olderModel, DiagramSourceCodeModel newerModel, int maxSDSize) throws Exception {

        List<BinaryClassRelationship> oldBinaryRelationships = new BinaryClassRelationshipExtractor<>()
                .generateBinaryClassRelationships(olderModel);
        List<BinaryClassRelationship> newBinaryRelationships = new BinaryClassRelationshipExtractor<>()
                .generateBinaryClassRelationships(newerModel);

        // form a list of all components that exist in the newer code base but
        // not in the older code base.
        List<String> addedComponents = new ArrayList<>();
        for (final Map.Entry<String, DiagramComponent> entry : newerModel.getComponents().entrySet()) {
            if (entry.getValue().componentType() != OOPSourceModelConstants.ComponentType.LOCAL
                && entry.getValue().componentType() != OOPSourceModelConstants.ComponentType.CONSTRUCTOR
                && !olderModel.containsComponent(entry.getKey())) {
                addedComponents.add(entry.getKey());
            }
        }

        // form a list of all base components that exist in the newer code base
        // but not in the older code base.
        Set<String> mainComponents = new HashSet<>();
        addedComponents.forEach(s -> {
            DiagramComponent cmp = newerModel.getComponent(s);
            if (cmp.componentType() != OOPSourceModelConstants.ComponentType.LOCAL) {
                while (cmp != null && !cmp.componentType().isBaseComponent()) {
                    cmp = newerModel.getComponent(cmp.parentUniqueName());
                }
                if (cmp != null && cmp.componentType().isBaseComponent()) {
                    mainComponents.add(cmp.uniqueName());
                }
            }
        });

        // form a list of all components that do not exist in the newer code
        // base but do exist in the older code base.
        List<String> deletedComponents = new ArrayList<>();
        for (final Map.Entry<String, DiagramComponent> entry : olderModel.getComponents().entrySet()) {
            if (entry.getValue().componentType() != OOPSourceModelConstants.ComponentType.LOCAL
                && entry.getValue().componentType() != OOPSourceModelConstants.ComponentType.CONSTRUCTOR
                && !newerModel.containsComponent(entry.getKey())) {
                deletedComponents.add(entry.getKey());
            }
        }

        // form a list of all base components that do not exist in the newer
        // code base but do exist in the older code base.
        deletedComponents.forEach(s -> {
            DiagramComponent cmp = olderModel.getComponent(s);
            if (cmp.componentType() != OOPSourceModelConstants.ComponentType.LOCAL) {
                while (cmp != null && !cmp.componentType().isBaseComponent()) {
                    cmp = olderModel.getComponent(cmp.parentUniqueName());
                }
                if (cmp != null && cmp.componentType().isBaseComponent()) {
                    mainComponents.add(cmp.uniqueName());
                }
            }
        });

        // form a list of all components that exist in both the old and new codebase,
        // but it's implementation differs between them.
        List<String> modifiedComponents = new ArrayList<>();
        for (final Map.Entry<String, DiagramComponent> entry : olderModel.getComponents().entrySet()) {
            if (entry.getValue().componentType().isMethodComponent()
                && newerModel.containsComponent(entry.getKey())
                && entry.getValue().componentType() != OOPSourceModelConstants.ComponentType.CONSTRUCTOR
                && !entry.getValue().code().equalsIgnoreCase(newerModel.getComponent(entry.getKey()).code())) {
                modifiedComponents.add(entry.getKey());
            }
        }

        // form a list of all base components that do not exist in the newer
        // code base but do exist in the older code base.
        modifiedComponents.forEach(s -> {
            DiagramComponent cmp = olderModel.getComponent(s);
            if (cmp.componentType() != OOPSourceModelConstants.ComponentType.LOCAL) {
                while (cmp != null && !cmp.componentType().isBaseComponent()) {
                    cmp = olderModel.getComponent(cmp.parentUniqueName());
                }
                if (cmp != null && cmp.componentType().isBaseComponent()) {
                    mainComponents.add(cmp.uniqueName());
                }
            }
        });

        // form a list of all binary relationships that exist in the newer code
        // base but not in the older code base.
        List<BinaryClassRelationship> addedRelationships = new ArrayList<>();
        for (BinaryClassRelationship entry : newBinaryRelationships) {
            if (!oldBinaryRelationships.contains(entry)) {
                addedRelationships.add(entry);
                int relationAStr = entry.getaSideAssociation().getStrength();
                int relationBStr = entry.getbSideAssociation().getStrength();
                if ((relationAStr + relationBStr) >= BinaryClassAssociation.AGGREGATION.getStrength()) {
                    mainComponents.add(entry.getClassA().uniqueName());
                    mainComponents.add(entry.getClassB().uniqueName());
                }
            }
        }

        // form a list of all binary relationships that do not exist in the
        // newer code base but do exist in the older code base.
        List<BinaryClassRelationship> deletedRelationships = new ArrayList<>();
        for (BinaryClassRelationship entry : oldBinaryRelationships) {
            if (!(newBinaryRelationships.contains(entry))) {
                deletedRelationships.add(entry);
                int relationAStr = entry.getaSideAssociation().getStrength();
                int relationBStr = entry.getbSideAssociation().getStrength();
                if ((relationAStr + relationBStr) >= BinaryClassAssociation.AGGREGATION.getStrength()) {
                    mainComponents.add(entry.getClassA().uniqueName());
                    mainComponents.add(entry.getClassB().uniqueName());
                }
            }
        }

        if (mainComponents.size() == 0) {
            throw new EmptySDException("No major structural differences found!");
        }

        // generate a list of binary relationships needed to draw the entire
        // diff diagram
        List<BinaryClassRelationship> allBinaryRelationships = new ArrayList<>();
        allBinaryRelationships.addAll(newBinaryRelationships);
        allBinaryRelationships.addAll(oldBinaryRelationships);

        // form the merged code base
        Map<String, DiagramComponent> mergedCodeBase = new MergedSourceCodeModel(olderModel.getComponents(),
                newerModel.getComponents()).set();
        // generate a list of components that are needed to draw the structure-diff
        Set<DiagramComponent> keyComponents = new RelatedBaseComponentsGroup(mergedCodeBase, allBinaryRelationships, mainComponents).components();

        if (keyComponents.size() > maxSDSize) {
            throw new SDTooLargeException("Diagram could not be drawn because it is too large!");
        }

        PUMLDiagramDescription diffClarityView = new StructureDiffPUMLDiagramDesciption(keyComponents,
                new HashSet<>(allBinaryRelationships), deletedRelationships, addedRelationships, deletedComponents, addedComponents,
                mergedCodeBase, colorScheme, modifiedComponents);
        this.diagram = new PUMLDiagram(diffClarityView, colorScheme, keyComponents.size());
    }

    @Override
    public Diagram view() {
        return this.diagram;
    }
}
