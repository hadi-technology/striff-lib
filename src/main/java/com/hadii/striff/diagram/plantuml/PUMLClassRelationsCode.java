package com.hadii.striff.diagram.plantuml;

import com.hadii.striff.diagram.ComponentHelper;
import com.hadii.striff.diagram.DiagramComponent;
import com.hadii.striff.extractor.DiagramConstants;
import com.hadii.striff.diagram.display.DiagramDisplay;
import com.hadii.striff.extractor.ComponentAssociationMultiplicity;
import com.hadii.striff.extractor.ComponentRelation;
import com.hadii.striff.extractor.RelationsMap;
import java.util.Set;
import java.util.stream.Collectors;

final class PUMLClassRelationsCode {

    private final Set<DiagramComponent> diagramComponents;
    private final RelationsMap diagramRels;
    private final RelationsMap addedRels;
    private final RelationsMap deletedRels;
    private final DiagramDisplay diagramDisplay;
    private StringBuilder tempStrBuilder;

    PUMLClassRelationsCode(PUMLDiagramData data) {
        this.diagramComponents = data.diagramCmps();
        this.diagramRels = data.diagramRels();
        this.addedRels = data.addedRels();
        this.deletedRels = data.deletedRels();
        this.diagramDisplay = data.diagramDisplay();
        genCode();
    }

    private String sanitizedCmpName(String cmpRef) {
        return cmpRef.replace("-", "").replaceAll("\\.\\.+", ".");
    }

    private void genCode() {
        this.tempStrBuilder = new StringBuilder();
        Set<String> diagramCmpNames = this.diagramComponents.stream().map(DiagramComponent::uniqueName)
                .collect(Collectors.toSet());
        for (DiagramComponent currCmp : this.diagramComponents) {
            for (ComponentRelation currCmpRel : this.diagramRels.significantRels(currCmp.uniqueName())) {
                // Ensure the relationship involves components from this diagram
                if (diagramCmpNames.contains(currCmpRel.targetComponent().uniqueName())) {
                    // Get reverse relation between componentA and component B... this may be empty.
                    ComponentRelation reverseRel = this.diagramRels.mostSignificantRelation(
                            currCmpRel.targetComponent(), currCmpRel.originalComponent());
                    if ((currCmp.name() != null) && ((currCmpRel.targetComponent().name() != null)
                            && !currCmp.uniqueName().contains("(")
                            && !currCmpRel.targetComponent().uniqueName().contains("("))) {
                        final DiagramConstants.ComponentAssociation aToBAssociation = currCmpRel.associationType();
                        final DiagramConstants.ComponentAssociation bToAAssociation = reverseRel.associationType();
                        // Insert original component name
                        this.tempStrBuilder
                                .append(sanitizedCmpName(
                                        ComponentHelper.packagePath(currCmpRel.originalComponent().pkg())))
                                .append(".")
                                .append(sanitizedCmpName(currCmpRel.originalComponent().name()))
                                .append(" ");
                        // Insert BtoA multiplicity if it's not a standard 0-1 multiplicity
                        ComponentAssociationMultiplicity bToAMultiplicity = reverseRel
                                .targetComponentRelationMultiplicity();
                        if (!bToAMultiplicity.value().isEmpty()
                                && !bToAMultiplicity.value()
                                        .equals(DiagramConstants.DefaultClassMultiplicities.ZEROTOONE.value())) {
                            this.tempStrBuilder.append("\"")
                                    .append(bToAMultiplicity.value())
                                    .append("\" ");
                        }
                        // If A aggregates or composes B, draw A's relationship arrow first...
                        if (aToBAssociation.equals(DiagramConstants.ComponentAssociation.COMPOSITION)
                                || aToBAssociation.equals(DiagramConstants.ComponentAssociation.AGGREGATION)) {
                            this.tempStrBuilder.append(aToBAssociation.getBackwardLinkEndingType());
                            // Otherwise, if B to A's association is not composition or aggregation either,
                            // draw B's association next...
                        } else if (!bToAAssociation.equals(DiagramConstants.ComponentAssociation.COMPOSITION)
                                && !bToAAssociation.equals(DiagramConstants.ComponentAssociation.AGGREGATION)) {
                            this.tempStrBuilder.append(bToAAssociation.getBackwardLinkEndingType());
                            // Otherwise draw an empty line
                        } else {
                            this.tempStrBuilder
                                    .append(DiagramConstants.ComponentAssociation.NONE.getBackwardLinkEndingType());
                        }
                        // Draw arrow middle section next
                        if (aToBAssociation.strength() > bToAAssociation.strength()) {
                            this.tempStrBuilder.append(
                                    new PUMLRelText(aToBAssociation, arrowDiffColor(
                                            currCmpRel, addedRels, deletedRels)).text());
                        } else {
                            this.tempStrBuilder.append(
                                    new PUMLRelText(bToAAssociation, arrowDiffColor(
                                            reverseRel, addedRels, deletedRels)).text());
                        }
                        // If B aggregates or composes A, draw B's relationship arrow next...
                        if (bToAAssociation.equals(DiagramConstants.ComponentAssociation.COMPOSITION)
                                || bToAAssociation.equals(DiagramConstants.ComponentAssociation.AGGREGATION)) {
                            this.tempStrBuilder.append(bToAAssociation.getForwardLinkEndingType());
                            // Otherwise, if A's to B's association is not composition or aggregation
                            // either, draw A's association next...
                        } else if (!aToBAssociation.equals(DiagramConstants.ComponentAssociation.COMPOSITION)
                                && !aToBAssociation.equals(DiagramConstants.ComponentAssociation.AGGREGATION)) {
                            this.tempStrBuilder.append(aToBAssociation.getForwardLinkEndingType());
                            // Otherwise, draw an empty line
                        } else {
                            this.tempStrBuilder
                                    .append(DiagramConstants.ComponentAssociation.NONE.getForwardLinkEndingType());
                        }
                        // Insert AtoB multiplicity if it's not a standard 0-1 multiplicity
                        ComponentAssociationMultiplicity aToBMultiplicity = currCmpRel
                                .targetComponentRelationMultiplicity();
                        if (!aToBMultiplicity.value().isEmpty()
                                && !aToBMultiplicity.value()
                                        .equals(DiagramConstants.DefaultClassMultiplicities.ZEROTOONE.value())) {
                            this.tempStrBuilder.append("\"")
                                    .append(aToBMultiplicity.value())
                                    .append("\" ");
                        }
                        // Insert target component name
                        this.tempStrBuilder
                                .append(sanitizedCmpName(
                                        ComponentHelper.packagePath(currCmpRel.targetComponent().pkg())))
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