package com.hadii.striff.diagram.plantuml;

import com.hadii.striff.diagram.DiagramComponent;
import com.hadii.striff.diagram.DiagramConstants;
import com.hadii.striff.diagram.scheme.DiagramColorScheme;
import com.hadii.striff.extractor.ColoredBinaryClassAssociation;
import com.hadii.striff.extractor.ComponentAssociationMultiplicity;
import com.hadii.striff.extractor.ComponentRelation;
import com.hadii.striff.extractor.ComponentRelations;

import java.util.Set;

final class PUMLClassRelationsDesc {

    private final Set<DiagramComponent> diagramComponents;
    private final ComponentRelations allRelations;
    private final ComponentRelations addedRelationships;
    private final ComponentRelations deletedRelationships;
    private final DiagramColorScheme colorScheme;
    private StringBuilder tempStrBuilder;

    public PUMLClassRelationsDesc(Set<DiagramComponent> diagramComponents, ComponentRelations allRelations,
                                  ComponentRelations deletedRelationships, ComponentRelations addedRelationships,
                                  DiagramColorScheme colorScheme) {
        this.diagramComponents = diagramComponents;
        this.allRelations = allRelations;
        this.addedRelationships = addedRelationships;
        this.deletedRelationships = deletedRelationships;
        this.colorScheme = colorScheme;
        calculateValue();
    }

    private void  calculateValue() {
        this.tempStrBuilder = new StringBuilder();
        this.diagramComponents.stream().filter(this.allRelations::hasRelationsforComponent).forEach(diagramComponent -> this.allRelations.componentRelations(diagramComponent).forEach(componentRelation -> {
            // Get reverse relation between componentA and component B... this may be empty.
            ComponentRelation reverseRelation = allRelations.reverseRelation(componentRelation);
            if ((diagramComponent.name() != null) && ((componentRelation.targetComponent().name() != null) &&
                    !diagramComponent.uniqueName().contains("(") && !componentRelation.targetComponent().uniqueName().contains("("))) {
                final DiagramConstants.ComponentAssociation AtoBAssociation = componentRelation.associationType();
                final DiagramConstants.ComponentAssociation BtoAAssociation = reverseRelation.associationType();
                // Start building our PlantUML string for A component side
                this.tempStrBuilder.append(componentRelation.originalComponent().uniqueName().replace("-", "").replaceAll("\\.\\.+", ".")).append(" ");
                // Insert BtoA multiplicity if its not a standard 0-1 multiplicity
                ComponentAssociationMultiplicity BtoAMultiplicity = reverseRelation.getTargetComponentRelationMultiplicity();
                if (!BtoAMultiplicity.value().isEmpty() &&
                        !BtoAMultiplicity.value().equals(DiagramConstants.DefaultClassMultiplicities.ZEROTOONE.value())) {
                    this.tempStrBuilder.append("\"").append(BtoAMultiplicity.value()).append("\" ");
                }
                // If A aggregates or composes B, draw A's relationship arrow first...
                if (AtoBAssociation.equals(DiagramConstants.ComponentAssociation.COMPOSITION) ||
                        AtoBAssociation.equals(DiagramConstants.ComponentAssociation.AGGREGATION)) {
                    this.tempStrBuilder.append(AtoBAssociation.getBackwardLinkEndingType());
                    // Otherwise, if B to A's association is not composition or aggregation either, draw B's association next...
                } else if (!BtoAAssociation.equals(DiagramConstants.ComponentAssociation.COMPOSITION) &&
                        !BtoAAssociation.equals(DiagramConstants.ComponentAssociation.AGGREGATION)) {
                    this.tempStrBuilder.append(BtoAAssociation.getBackwardLinkEndingType());
                    // Otherwise draw an empty line
                } else {
                    this.tempStrBuilder.append(DiagramConstants.ComponentAssociation.NONE.getBackwardLinkEndingType());
                }
                // Draw arrow middle section next
                if (AtoBAssociation.strength() > BtoAAssociation.strength()) {
                    this.tempStrBuilder.append(new ColoredBinaryClassAssociation(AtoBAssociation,
                            arrowDiffColor(componentRelation, addedRelationships, deletedRelationships)).getyumlLinkType());
                } else {
                    this.tempStrBuilder.append(new ColoredBinaryClassAssociation(BtoAAssociation,
                            arrowDiffColor(reverseRelation, addedRelationships, deletedRelationships)).getyumlLinkType());
                }
                // If B aggregates or composes A, draw B's relationship arrow nex...
                if (BtoAAssociation.equals(DiagramConstants.ComponentAssociation.COMPOSITION) ||
                        BtoAAssociation.equals(DiagramConstants.ComponentAssociation.AGGREGATION)) {
                    this.tempStrBuilder.append(BtoAAssociation.getForwardLinkEndingType());
                    // Otherwise, if A's to B's association is not composition or aggregation either, draw A's association next...
                } else if (!AtoBAssociation.equals(DiagramConstants.ComponentAssociation.COMPOSITION) &&
                        !AtoBAssociation.equals(DiagramConstants.ComponentAssociation.AGGREGATION)) {
                    this.tempStrBuilder.append(AtoBAssociation.getForwardLinkEndingType());
                    // Otherwise draw an empty line
                } else {
                    this.tempStrBuilder.append(DiagramConstants.ComponentAssociation.NONE.getForwardLinkEndingType());
                }
                // Insert AtoB multiplicity if its not a standard 0-1 multiplicity
                ComponentAssociationMultiplicity AtoBMultiplicity = componentRelation.getTargetComponentRelationMultiplicity();
                if (!AtoBMultiplicity.value().isEmpty() &&
                        !AtoBMultiplicity.value().equals(DiagramConstants.DefaultClassMultiplicities.ZEROTOONE.value())) {
                    this.tempStrBuilder.append("\"").append(AtoBMultiplicity.value()).append("\" ");
                }
                // Insert component B name
                this.tempStrBuilder.append(" ").append(componentRelation.targetComponent().uniqueName().replaceAll("-", "").replaceAll("\\.\\.+", "."));
                this.tempStrBuilder.append("\n");
            }
        }));
    }

    private String arrowDiffColor(ComponentRelation relation, ComponentRelations addedRelationships,
                                  ComponentRelations deletedRelationships) {
        if (addedRelationships.hasRelation(relation)) {
            return colorScheme.addedRelationColor();
        } else if (deletedRelationships.hasRelation(relation)) {
            return colorScheme.deletedRelationColor();
        } else {
            return colorScheme.classArrowColor();
        }
    }

    public String value() {
        return this.tempStrBuilder.toString();
    }
}