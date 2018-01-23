package com.clarity.binary.diagram.view;

import com.clarity.binary.MergedSourceCodeModel;
import com.clarity.binary.diagram.Diagram;
import com.clarity.binary.diagram.DiagramConstants.BinaryClassAssociation;
import com.clarity.binary.diagram.RelatedBaseComponentsGroup;
import com.clarity.binary.diagram.plantuml.PUMLDiagram;
import com.clarity.binary.diagram.plantuml.PUMLDiagramDescription;
import com.clarity.binary.diagram.plantuml.StructureDiffPUMLDiagramDesciption;
import com.clarity.binary.diagram.scheme.DiagramColorScheme;
import com.clarity.binary.extractor.BinaryClassRelationship;
import com.clarity.binary.extractor.BinaryClassRelationshipExtractor;
import com.clarity.sourcemodel.Component;
import com.clarity.sourcemodel.OOPSourceCodeModel;
import com.clarity.sourcemodel.OOPSourceModelConstants;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
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

    public SDView(DiagramColorScheme colorScheme, OOPSourceCodeModel olderModel, OOPSourceCodeModel newerModel, int maxSDSize) throws Exception {

        Map<String, BinaryClassRelationship> oldBinaryRelationships = new BinaryClassRelationshipExtractor<Object>()
                .generateBinaryClassRelationships(olderModel);
        Map<String, BinaryClassRelationship> newBinaryRelationships = new BinaryClassRelationshipExtractor<Object>()
                .generateBinaryClassRelationships(newerModel);

        // form a list of all components that exist in the newer code base but
        // not in the older code base.
        List<String> addedComponents = new ArrayList<String>();
        for (final Map.Entry<String, Component> entry : newerModel.getComponents().entrySet()) {
            if (entry.getValue().componentType() != OOPSourceModelConstants.ComponentType.LOCAL && !olderModel.containsComponent(entry.getKey())) {
                addedComponents.add(entry.getKey());
            }
        }

        // form a list of all base components that exist in the newer code base
        // but not in the older code base.
        Set<String> mainComponents = new HashSet<String>();
        addedComponents.forEach(s -> {
            Component cmp = newerModel.getComponent(s);
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
        List<String> deletedComponents = new ArrayList<String>();
        for (final Map.Entry<String, Component> entry : olderModel.getComponents().entrySet()) {
            if (entry.getValue().componentType() != OOPSourceModelConstants.ComponentType.LOCAL && !newerModel.containsComponent(entry.getKey())) {
                deletedComponents.add(entry.getKey());
            }
        }

        // form a list of all base components that do not exist in the newer
        // code base but do exist in the older code base.
        deletedComponents.forEach(s -> {
            Component cmp = olderModel.getComponent(s);
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
        List<BinaryClassRelationship> addedRelationships = new ArrayList<BinaryClassRelationship>();
        for (final Map.Entry<String, BinaryClassRelationship> entry : newBinaryRelationships.entrySet()) {
            if (!oldBinaryRelationships.containsValue(entry.getValue())) {
                BinaryClassRelationship relation = entry.getValue();
                addedRelationships.add(relation);
                int relationAStr = relation.getaSideAssociation().getStrength();
                int relationBStr = relation.getbSideAssociation().getStrength();
                if ((relationAStr + relationBStr) >= BinaryClassAssociation.AGGREGATION.getStrength()) {
                    mainComponents.add(relation.getClassA().uniqueName());
                    mainComponents.add(relation.getClassB().uniqueName());
                }
            }
        }

        // form a list of all binary relationships that do not exist in the
        // newer code base but do exist in the older code base.
        List<BinaryClassRelationship> deletedRelationships = new ArrayList<BinaryClassRelationship>();
        for (final Map.Entry<String, BinaryClassRelationship> entry : oldBinaryRelationships.entrySet()) {
            if (!(newBinaryRelationships.containsValue(entry.getValue()))) {
                BinaryClassRelationship relation = entry.getValue();
                deletedRelationships.add(relation);
                int relationAStr = relation.getaSideAssociation().getStrength();
                int relationBStr = relation.getbSideAssociation().getStrength();
                if ((relationAStr + relationBStr) >= BinaryClassAssociation.AGGREGATION.getStrength()) {
                    mainComponents.add(relation.getClassA().uniqueName());
                    mainComponents.add(relation.getClassB().uniqueName());
                }
            }
        }

        if (mainComponents.size() == 0) {
            throw new EmptySDException("No major structural differences found!");
        }

        // generate a list of binary relationships needed to draw the entire
        // diff diagram
        Map<String, BinaryClassRelationship> allBinaryRelationships = new HashMap<>();
        allBinaryRelationships.putAll(newBinaryRelationships);
        allBinaryRelationships.putAll(oldBinaryRelationships);

        // form the merged code base
        Map<String, Component> mergedCodeBase = new MergedSourceCodeModel(olderModel.getComponents(),
                newerModel.getComponents()).set();
        // generate a list of components that are needed to draw the structure-diff
        Set<Component> keyComponents = new RelatedBaseComponentsGroup(mergedCodeBase, allBinaryRelationships, mainComponents).components();

        if (keyComponents.size() > maxSDSize) {
            throw new SDTooLargeException("Diagram could not be drawn because it is too large!");
        }

        PUMLDiagramDescription diffClarityView = new StructureDiffPUMLDiagramDesciption(keyComponents,
                new HashSet<>(allBinaryRelationships.values()), deletedRelationships, addedRelationships, deletedComponents, addedComponents,
                mergedCodeBase, colorScheme);
        this.diagram = new PUMLDiagram(diffClarityView, colorScheme, keyComponents.size());

    }

    @Override
    public Diagram view() {
        return this.diagram;
    }

    private int noStrongRelations(List<BinaryClassRelationship> relations) {
        int count = 0;
        for (BinaryClassRelationship relation : relations) {
            if (relation.getaSideAssociation().getStrength() >= BinaryClassAssociation.ASSOCIATION.getStrength()) {
                count++;
            } else if (relation.getbSideAssociation().getStrength() >= BinaryClassAssociation.ASSOCIATION.getStrength()) {
                count++;
            }
        }
        return count;
    }
}
