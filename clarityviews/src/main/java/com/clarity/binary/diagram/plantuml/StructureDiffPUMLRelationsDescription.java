package com.clarity.binary.diagram.plantuml;

import com.clarity.binary.diagram.DiagramComponent;
import com.clarity.binary.diagram.DiagramConstants;
import com.clarity.binary.diagram.scheme.DiagramColorScheme;
import com.clarity.binary.extractor.BinaryClassRelationship;
import com.clarity.binary.extractor.ColoredBinaryClassAssociation;

import java.util.List;
import java.util.Set;

public class StructureDiffPUMLRelationsDescription {

    private final Set<DiagramComponent> diagramComponents;
    private final Set<BinaryClassRelationship> binaryRelationships;
    private final List<BinaryClassRelationship> addedRelationships;
    private final List<BinaryClassRelationship> deletedRelationships;
    private final List<String> modifiedComponents;
    private final DiagramColorScheme colorScheme;

    public StructureDiffPUMLRelationsDescription(Set<DiagramComponent> diagramComponents,
                                                 Set<BinaryClassRelationship> allRelationships, List<BinaryClassRelationship> deletedRelationships,
                                                 List<BinaryClassRelationship> addedRelationships, DiagramColorScheme colorScheme,
                                                 List<String> modifiedComponents) {
        this.diagramComponents = diagramComponents;
        this.binaryRelationships = allRelationships;
        this.addedRelationships = addedRelationships;
        this.deletedRelationships = deletedRelationships;
        this.modifiedComponents = modifiedComponents;
        this.colorScheme = colorScheme;
    }

    public String value() {
        final StringBuilder tempStrBuilder = new StringBuilder();
        for (BinaryClassRelationship relationship : binaryRelationships) {

            String classAName = relationship.getClassA().name();
            String classBName = relationship.getClassB().name();

            if ((classAName != null) && ((classBName != null) && !relationship.getClassA().uniqueName().contains("(")
                    && !relationship.getClassB().uniqueName().contains("(")
                    && diagramComponents.contains(relationship.getClassA())
                    && diagramComponents.contains(relationship.getClassB())
                    && relationship.getClassA().componentType().isBaseComponent()
                    && relationship.getClassB().componentType().isBaseComponent() && (relationship != null))) {
                final DiagramConstants.BinaryClassAssociation classAAssociation = relationship.getaSideAssociation();
                final DiagramConstants.BinaryClassAssociation classBAssociation = relationship.getbSideAssociation();
                // start building our string for side class A
                // insert class A short name
                tempStrBuilder.append(relationship.getClassA().uniqueName().replace("-", "").replaceAll("\\.\\.+", ".") + " ");
                // insert class B multiplicity if its not a zero to one
                // multiplicity..
                if (!relationship.getbSideMultiplicity().getValue().isEmpty() && !relationship.getbSideMultiplicity()
                        .getValue().equals(DiagramConstants.DefaultClassMultiplicities.ZEROTOONE.getValue())) {
                    tempStrBuilder.append("\"" + relationship.getbSideMultiplicity().getValue() + "\" ");
                }
                // insert class B association type
                tempStrBuilder.append(classBAssociation.getBackwardLinkEndingType());

                if (classAAssociation.getStrength() > classBAssociation.getStrength()) {
                    tempStrBuilder.append(new ColoredBinaryClassAssociation(classAAssociation,
                            arrowDiffColor(relationship, addedRelationships, deletedRelationships)).getyumlLinkType());
                } else {
                    tempStrBuilder.append(new ColoredBinaryClassAssociation(classBAssociation,
                            arrowDiffColor(relationship, addedRelationships, deletedRelationships)).getyumlLinkType());
                }
                // insert class A association type
                tempStrBuilder.append(classAAssociation.getForwardLinkEndingType());
                // insert class A multiplicity if its not a zero to one
                // multiplicity..
                if (!relationship.getaSideMultiplicity().getValue().isEmpty() && !relationship.getaSideMultiplicity()
                        .getValue().equals(DiagramConstants.DefaultClassMultiplicities.ZEROTOONE.getValue())) {
                    tempStrBuilder.append(" \"" + relationship.getaSideMultiplicity().getValue() + "\" ");
                }
                // insert class B name
                tempStrBuilder.append(" " + relationship.getClassB().uniqueName().replaceAll("-", "").replaceAll("\\.\\.+", "."));
                tempStrBuilder.append("\n");
            }
        }
        return tempStrBuilder.toString();
    }

    private String arrowDiffColor(BinaryClassRelationship relation, List<BinaryClassRelationship> addedRelationships,
                                  List<BinaryClassRelationship> deletedRelationships) {
        for (BinaryClassRelationship bCR : addedRelationships) {
            if (bCR.equals(relation)) {
                return colorScheme.addedRelationColor();
            }
        }
        for (BinaryClassRelationship bCR : deletedRelationships) {
            if (bCR.equals(relation)) {
                return colorScheme.deletedRelationColor();
            }
        }
        return colorScheme.classArrowColor();
    }
}