package com.hadii.striff.diagram.plantuml;

import com.hadii.striff.diagram.DiagramComponent;
import com.hadii.striff.extractor.DiagramConstants;
import com.hadii.striff.diagram.display.DiagramDisplay;
import com.hadii.striff.extractor.ComponentAssociationMultiplicity;
import com.hadii.striff.extractor.ComponentRelation;
import com.hadii.striff.extractor.RelationsMap;
import com.hadii.striff.parse.CodeDiff;

import java.util.Set;

final class PUMLClassRelationsCode {

    private final Set<DiagramComponent> diagramComponents;
    private final RelationsMap allRelations;
    private final RelationsMap addedRelationships;
    private final RelationsMap deletedRelationships;
    private final DiagramDisplay diagramDisplay;
    private StringBuilder tempStrBuilder;

    PUMLClassRelationsCode(Set<DiagramComponent> diagramComponents, CodeDiff mergedModel,
                                  DiagramDisplay diagramDisplay) {
        this.diagramComponents = diagramComponents;
        this.allRelations = mergedModel.extractedRels();
        this.addedRelationships = mergedModel.changeSet().addedRelations();
        this.deletedRelationships = mergedModel.changeSet().deletedRelations();
        this.diagramDisplay = diagramDisplay;
        genCode();
    }

    private String sanitizedCmpName(String cmpRef) {
        return cmpRef.replace("-", "").replaceAll("\\.\\.+", ".");
    }
    private void genCode() {
        this.tempStrBuilder = new StringBuilder();
        for (DiagramComponent currCmp : this.diagramComponents) {
                for (ComponentRelation currCmpRel : this.allRelations.significantRels(currCmp)) {
                    // Ensure the relationship involves components from this diagram
                    if (this.diagramComponents.contains(currCmpRel.targetComponent())) {
                        // Get reverse relation between componentA and component B... this may be empty.
                        ComponentRelation reverseRel = this.allRelations.mostSignificantRelation(
                            currCmpRel.targetComponent(), currCmpRel.originalComponent());
                        if ((currCmp.name() != null) && ((currCmpRel.targetComponent().name() != null)
                                && !currCmp.uniqueName().contains("(") && !currCmpRel.targetComponent().uniqueName().contains("("))) {
                            final DiagramConstants.ComponentAssociation aToBAssociation = currCmpRel.associationType();
                            final DiagramConstants.ComponentAssociation bToAAssociation = reverseRel.associationType();
                            // Insert original component name
                            this.tempStrBuilder.append(sanitizedCmpName(currCmpRel.originalComponent().packagePath()))
                                               .append(".")
                                               .append(sanitizedCmpName(currCmpRel.originalComponent().name()))
                                               .append(" ");
                            // Insert BtoA multiplicity if it's not a standard 0-1 multiplicity
                            ComponentAssociationMultiplicity bToAMultiplicity = reverseRel.getTargetComponentRelationMultiplicity();
                            if (!bToAMultiplicity.value().isEmpty()
                                    && !bToAMultiplicity.value().equals(DiagramConstants.DefaultClassMultiplicities.ZEROTOONE.value())) {
                                this.tempStrBuilder.append("\"")
                                                   .append(bToAMultiplicity.value())
                                                   .append("\" ");
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
                                this.tempStrBuilder.append(
                                    new PUMLRelText(aToBAssociation, arrowDiffColor(
                                        currCmpRel, addedRelationships, deletedRelationships)).text());
                            } else {
                                this.tempStrBuilder.append(
                                    new PUMLRelText(bToAAssociation, arrowDiffColor(
                                        reverseRel, addedRelationships, deletedRelationships)).text());
                            }
                            // If B aggregates or composes A, draw B's relationship arrow next...
                            if (bToAAssociation.equals(DiagramConstants.ComponentAssociation.COMPOSITION)
                                    || bToAAssociation.equals(DiagramConstants.ComponentAssociation.AGGREGATION)) {
                                this.tempStrBuilder.append(bToAAssociation.getForwardLinkEndingType());
                                // Otherwise, if A's to B's association is not composition or aggregation either, draw A's association next...
                            } else if (!aToBAssociation.equals(DiagramConstants.ComponentAssociation.COMPOSITION)
                                    && !aToBAssociation.equals(DiagramConstants.ComponentAssociation.AGGREGATION)) {
                                this.tempStrBuilder.append(aToBAssociation.getForwardLinkEndingType());
                                // Otherwise, draw an empty line
                            } else {
                                this.tempStrBuilder.append(DiagramConstants.ComponentAssociation.NONE.getForwardLinkEndingType());
                            }
                            // Insert AtoB multiplicity if it's not a standard 0-1 multiplicity
                            ComponentAssociationMultiplicity aToBMultiplicity = currCmpRel.getTargetComponentRelationMultiplicity();
                            if (!aToBMultiplicity.value().isEmpty()
                                    && !aToBMultiplicity.value().equals(DiagramConstants.DefaultClassMultiplicities.ZEROTOONE.value())) {
                                this.tempStrBuilder.append("\"")
                                                   .append(aToBMultiplicity.value())
                                                   .append("\" ");
                            }
                            // Insert target component name
                            this.tempStrBuilder.append(sanitizedCmpName(currCmpRel.targetComponent().packagePath()))
                                               .append(".")
                                               .append(sanitizedCmpName(currCmpRel.targetComponent().name()))
                                               .append(" ");
                            this.tempStrBuilder.append("\n");
                        }
                    }
                }
        }
    }

    private String arrowDiffColor(ComponentRelation relation, RelationsMap addedRelationships,
                                  RelationsMap deletedRelationships) {
        if (addedRelationships.contains(relation)) {
            return this.diagramDisplay.colorScheme().addedRelationColor();
        } else if (deletedRelationships.contains(relation)) {
            return this.diagramDisplay.colorScheme().deletedRelationColor();
        } else {
            return this.diagramDisplay.colorScheme().classArrowColor();
        }
    }

    public String value() {
        return this.tempStrBuilder.toString();
    }
}