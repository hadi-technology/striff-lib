package com.hadii.striff.diagram.plantuml;

import com.hadii.striff.diagram.DiagramComponent;
import com.hadii.striff.diagram.DiagramConstants;
import com.hadii.striff.diagram.scheme.DiagramColorScheme;
import com.hadii.striff.extractor.ColoredBinaryClassAssociation;
import com.hadii.striff.extractor.ComponentAssociationMultiplicity;
import com.hadii.striff.extractor.ComponentRelation;
import com.hadii.striff.extractor.ComponentRelations;
import com.hadii.striff.parse.DiffCodeModel;

import java.util.Set;

final class PUMLClassRelationsCode {

    private final Set<DiagramComponent> diagramComponents;
    private final ComponentRelations allRelations;
    private final ComponentRelations addedRelationships;
    private final ComponentRelations deletedRelationships;
    private final DiagramColorScheme colorScheme;
    private StringBuilder tempStrBuilder;

    PUMLClassRelationsCode(Set<DiagramComponent> diagramComponents, DiffCodeModel mergedModel,
                                  DiagramColorScheme colorScheme) {
        this.diagramComponents = diagramComponents;
        this.allRelations = mergedModel.relations();
        this.addedRelationships = mergedModel.changeSet().addedRelations();
        this.deletedRelationships = mergedModel.changeSet().deletedRelations();
        this.colorScheme = colorScheme;
        genCode();
    }

    private void genCode() {
        this.tempStrBuilder = new StringBuilder();
        for (DiagramComponent diagramComponent : this.diagramComponents) {
            if (this.allRelations.hasRelationsforComponent(diagramComponent)) {
                for (ComponentRelation componentRelation : this.allRelations.componentRelations(diagramComponent)) {
                    // Ensure the relationship involves components from this diagram
                    if (this.diagramComponents.contains(componentRelation.targetComponent())) {
                        // Get reverse relation between componentA and component B... this may be empty.
                        ComponentRelation reverseRelation = this.allRelations.reverseRelation(componentRelation);
                        if ((diagramComponent.name() != null) && ((componentRelation.targetComponent().name() != null)
                                && !diagramComponent.uniqueName().contains("(") && !componentRelation.targetComponent().uniqueName().contains("("))) {
                            final DiagramConstants.ComponentAssociation aToBAssociation = componentRelation.associationType();
                            final DiagramConstants.ComponentAssociation bToAAssociation = reverseRelation.associationType();
                            // Start building our PlantUML string for A component side
                            this.tempStrBuilder.append(componentRelation.originalComponent().uniqueName().replace("-", "").replaceAll("\\.\\.+", ".")).append(" ");
                            // Insert BtoA multiplicity if its not a standard 0-1 multiplicity
                            ComponentAssociationMultiplicity bToAMultiplicity = reverseRelation.getTargetComponentRelationMultiplicity();
                            if (!bToAMultiplicity.value().isEmpty()
                                    && !bToAMultiplicity.value().equals(DiagramConstants.DefaultClassMultiplicities.ZEROTOONE.value())) {
                                this.tempStrBuilder.append("\"").append(bToAMultiplicity.value()).append("\" ");
                            }
                            // If A aggregates or composes B, draw A's relationship arrow first...
                            if (aToBAssociation.equals(DiagramConstants.ComponentAssociation.COMPOSITION)
                                    || aToBAssociation.equals(DiagramConstants.ComponentAssociation.AGGREGATION)) {
                                this.tempStrBuilder.append(aToBAssociation.getBackwardLinkEndingType());
                                // Otherwise, if B to A's association is not composition or aggregation either, draw B's association next...
                            } else if (!bToAAssociation.equals(DiagramConstants.ComponentAssociation.COMPOSITION)
                                    && !bToAAssociation.equals(DiagramConstants.ComponentAssociation.AGGREGATION)) {
                                this.tempStrBuilder.append(bToAAssociation.getBackwardLinkEndingType());
                                // Otherwise draw an empty line
                            } else {
                                this.tempStrBuilder.append(DiagramConstants.ComponentAssociation.NONE.getBackwardLinkEndingType());
                            }
                            // Draw arrow middle section next
                            if (aToBAssociation.strength() > bToAAssociation.strength()) {
                                this.tempStrBuilder.append(new ColoredBinaryClassAssociation(aToBAssociation,
                                        arrowDiffColor(componentRelation, addedRelationships, deletedRelationships)).getyumlLinkType());
                            } else {
                                this.tempStrBuilder.append(new ColoredBinaryClassAssociation(bToAAssociation,
                                        arrowDiffColor(reverseRelation, addedRelationships, deletedRelationships)).getyumlLinkType());
                            }
                            // If B aggregates or composes A, draw B's relationship arrow next...
                            if (bToAAssociation.equals(DiagramConstants.ComponentAssociation.COMPOSITION)
                                    || bToAAssociation.equals(DiagramConstants.ComponentAssociation.AGGREGATION)) {
                                this.tempStrBuilder.append(bToAAssociation.getForwardLinkEndingType());
                                // Otherwise, if A's to B's association is not composition or aggregation either, draw A's association next...
                            } else if (!aToBAssociation.equals(DiagramConstants.ComponentAssociation.COMPOSITION)
                                    && !aToBAssociation.equals(DiagramConstants.ComponentAssociation.AGGREGATION)) {
                                this.tempStrBuilder.append(aToBAssociation.getForwardLinkEndingType());
                                // Otherwise draw an empty line
                            } else {
                                this.tempStrBuilder.append(DiagramConstants.ComponentAssociation.NONE.getForwardLinkEndingType());
                            }
                            // Insert AtoB multiplicity if its not a standard 0-1 multiplicity
                            ComponentAssociationMultiplicity aToBMultiplicity = componentRelation.getTargetComponentRelationMultiplicity();
                            if (!aToBMultiplicity.value().isEmpty()
                                    && !aToBMultiplicity.value().equals(DiagramConstants.DefaultClassMultiplicities.ZEROTOONE.value())) {
                                this.tempStrBuilder.append("\"").append(aToBMultiplicity.value()).append("\" ");
                            }
                            // Insert component B name
                            this.tempStrBuilder.append(" ").append(componentRelation.targetComponent().uniqueName().replaceAll("-", "").replaceAll("\\.\\.+", "."));
                            this.tempStrBuilder.append("\n");
                        }
                    }
                }
            }
        }
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