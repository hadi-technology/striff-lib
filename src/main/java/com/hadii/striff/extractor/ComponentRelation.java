package com.hadii.striff.extractor;

import com.hadii.striff.diagram.DiagramComponent;
import com.hadii.striff.extractor.DiagramConstants.ComponentAssociation;

/**
 * Represents a relation between a source and target
 * {@link com.hadii.clarpse.sourcemodel.Component} in a code base.
 */
public class ComponentRelation implements Comparable<ComponentRelation> {

    private DiagramComponent originalComponent;
    private DiagramComponent targetComponent;
    // linkTargetMultiplicity of the link…
    private ComponentAssociationMultiplicity targetComponentRelationMultiplicity =
        new ComponentAssociationMultiplicity(DiagramConstants.DefaultClassMultiplicities.NONE);
    private ComponentAssociation associationType = ComponentAssociation.NONE;

    public ComponentRelation(final DiagramComponent originalComponent,
                             final DiagramComponent targetComponent,
                             final ComponentAssociationMultiplicity targetComponentRelationMultiplicity,
                             final ComponentAssociation associationType) {
        validateRelCmpTypes(originalComponent, targetComponent);
        this.targetComponentRelationMultiplicity = (targetComponentRelationMultiplicity);
        this.originalComponent = (originalComponent);
        this.targetComponent = (targetComponent);
        this.associationType = (associationType);
    }

    private void validateRelCmpTypes(DiagramComponent originalCmp, DiagramComponent targetCmp) {
        if (!targetCmp.componentType().isBaseComponent()) {
            throw new IllegalArgumentException("Target component " + targetCmp.uniqueName()
                                                   + " is not a base component!");
        }
        if (!originalCmp.componentType().isBaseComponent()) {
            throw new IllegalArgumentException("Original component " + originalCmp.uniqueName()
                                                   + " is not a base component!");
        }
    }

    public ComponentRelation() {
    }

    @Override
    public String toString() {
        return "ComponentRelation{"
            + "originalComponent=" + originalComponent.name()
            + ", targetComponent=" + targetComponent.name()
            + ", targetMultiplicity=" + targetComponentRelationMultiplicity.value()
            + ", associationType=" + associationType.name()
            + '}';
    }

    public int strength() {
        return this.associationType.strength();
    }

    public ComponentAssociationMultiplicity getTargetComponentRelationMultiplicity() {
        return targetComponentRelationMultiplicity;
    }


    public DiagramComponent targetComponent() {
        return targetComponent;
    }

    public DiagramComponent originalComponent() {
        return originalComponent;
    }

    public ComponentAssociation associationType() {
        return associationType;
    }

    @Override
    public int hashCode() {
        return (originalComponent.uniqueName() + "->" + targetComponent.uniqueName()).hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ComponentRelation)) {
            return false;
        }
        ComponentRelation other = (ComponentRelation) obj;
        return other.originalComponent().equals(originalComponent())
            && other.targetComponent().equals(targetComponent())
            && other.associationType.name().equals(this.associationType.name());
    }

    @Override
    public int compareTo(ComponentRelation relation) {
        if (this.strength() < relation.strength()) {
            return 1;
        } else if (this.strength() > relation.strength()) {
            return -1;
        } else {
            return 0;
        }
    }
}
