package com.hadi.striff.diagram.plantuml;

import com.hadi.striff.diagram.DiagramComponent;
import com.hadi.striff.extractor.DiagramConstants;
import com.hadi.striff.diagram.display.DiagramDisplay;
import com.hadi.striff.extractor.ComponentAssociationMultiplicity;
import com.hadi.striff.extractor.ComponentRelation;
import com.hadi.striff.extractor.RelationsMap;
import com.hadi.striff.spi.RelationDecorator;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

final class PUMLClassRelationsCode {

    private final Set<DiagramComponent> diagramComponents;
    private final RelationsMap diagramRels;
    private final RelationsMap addedRels;
    private final RelationsMap deletedRels;
    private final DiagramDisplay diagramDisplay;
    private final List<RelationDecorator> relationDecorators;
    private StringBuilder tempStrBuilder;

    PUMLClassRelationsCode(PUMLDiagramData data) {
        this.diagramComponents = data.diagramCmps();
        this.diagramRels = data.diagramRels();
        this.addedRels = data.addedRels();
        this.deletedRels = data.deletedRels();
        this.diagramDisplay = data.diagramDisplay();
        this.relationDecorators = data.relationDecorators();
        genCode();
    }

    private void genCode() {
        this.tempStrBuilder = new StringBuilder();
        Set<String> diagramCmpNames = this.diagramComponents.stream().map(DiagramComponent::uniqueName)
                .collect(Collectors.toSet());
        Set<String> renderedPairs = new HashSet<>();
        for (DiagramComponent currCmp : this.diagramComponents) {
            for (ComponentRelation currCmpRel : this.diagramRels.significantRels(currCmp.uniqueName())) {
                // Ensure the relationship involves components from this diagram
                if (diagramCmpNames.contains(currCmpRel.targetComponent().uniqueName())) {
                    // Skip if the reverse pair was already rendered
                    String pair = pairKey(currCmpRel.originalComponent().uniqueName(),
                            currCmpRel.targetComponent().uniqueName());
                    if (!renderedPairs.add(pair)) {
                        continue;
                    }
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
                                .append("\"")
                                .append(PUMLHelper.pumlId(currCmpRel.originalComponent().uniqueName()))
                                .append("\" ");
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
                                .append("\"")
                                .append(PUMLHelper.pumlId(currCmpRel.targetComponent().uniqueName()))
                                .append("\" ");
                        this.tempStrBuilder.append("\n");
                        appendRelationDecorations(currCmpRel, reverseRel);
                    }
                }
            }
        }
    }

    private void appendRelationDecorations(ComponentRelation relation, ComponentRelation reverseRelation) {
        for (RelationDecorator decorator : relationDecorators) {
            List<String> extra = decorator.decorateRelation(relation, reverseRelation, diagramDisplay);
            if (extra == null || extra.isEmpty()) {
                continue;
            }
            for (String line : extra) {
                tempStrBuilder.append(line);
                if (!line.endsWith("\n")) {
                    tempStrBuilder.append("\n");
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

    /**
     * Creates a canonical key for a pair of components so that (A,B) and (B,A)
     * produce the same key, enabling deduplication of bidirectional arrows.
     */
    private String pairKey(String a, String b) {
        return a.compareTo(b) < 0 ? a + "|" + b : b + "|" + a;
    }

    public String value() {
        return this.tempStrBuilder.toString();
    }
}
