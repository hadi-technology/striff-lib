package com.hadii.striff.extractor;

import com.hadii.striff.diagram.DiagramComponent;
import com.hadii.striff.diagram.DiagramConstants;
import com.hadii.striff.diagram.DiagramConstants.ComponentAssociation;

/**
 * Represents a relation between a source and target {@link com.hadii.clarpse.sourcemodel.Component} in a code base.
 */
public class ComponentRelation {

    private DiagramComponent originalComponent;
    private DiagramComponent targetComponent;
    // linkTargetMultiplicity of the link..
    private ComponentAssociationMultiplicity targetComponentRelationMultiplicity =
            new ComponentAssociationMultiplicity(DiagramConstants.DefaultClassMultiplicities.NONE);
    private ComponentAssociation associationType = ComponentAssociation.NONE;

    public ComponentRelation(final DiagramComponent originalComponent, final DiagramComponent targetComponent,
                             final ComponentAssociationMultiplicity targetComponentRelationMultiplicity,
                             final ComponentAssociation associationType) {

        this.targetComponentRelationMultiplicity = (targetComponentRelationMultiplicity);
        this.originalComponent = (originalComponent);
        this.targetComponent = (targetComponent);
        this.associationType = (associationType);
    }

    public ComponentRelation() { }

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
        return (originalComponent.uniqueName() + targetComponent.uniqueName()).hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (!(obj instanceof ComponentRelation))
            return false;
        ComponentRelation other = (ComponentRelation) obj;
        return other.originalComponent.equals(this.originalComponent) && other.targetComponent.equals(this.targetComponent);
    }
}
