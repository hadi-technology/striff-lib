package com.hadi.striff.extractor;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.hadi.clarpse.sourcemodel.Component;
import com.hadi.striff.diagram.SyntheticModuleSupport;
import com.hadi.striff.extractor.DiagramConstants.ComponentAssociation;

import java.io.Serial;
import java.io.Serializable;

/**
 * Represents a relation between a source and target {@link Component} in a code
 * base.
 */
public class ComponentRelation implements Comparable<ComponentRelation>, Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Component originalComponent;
    private Component targetComponent;
    private ComponentAssociationMultiplicity targetComponentRelationMultiplicity = new ComponentAssociationMultiplicity(
            DiagramConstants.DefaultClassMultiplicities.NONE);
    private ComponentAssociation associationType = ComponentAssociation.NONE;

    public ComponentRelation(
            @JsonProperty("originalComponent") Component originalComponent,
            @JsonProperty("targetComponent") Component targetComponent,
            @JsonProperty("targetComponentRelationMultiplicity") ComponentAssociationMultiplicity targetComponentRelationMultiplicity,
            @JsonProperty("associationType") ComponentAssociation associationType) {
        validateRelCmpTypes(originalComponent, targetComponent);
        this.originalComponent = originalComponent;
        this.targetComponent = targetComponent;
        this.targetComponentRelationMultiplicity = targetComponentRelationMultiplicity;
        this.associationType = associationType;
    }

    public ComponentRelation() {
    }

    /**
     * Creates a ComponentRelation for a synthetic module, bypassing the base component
     * validation for the original component. This is used internally when creating
     * relations from synthetic modules to their dependencies.
     *
     * @param originalComponent the synthetic module component
     * @param targetComponent the target component (must be a base component)
     * @param targetComponentRelationMultiplicity the multiplicity
     * @param associationType the association type
     * @return a new ComponentRelation instance
     */
    public static ComponentRelation forSyntheticModule(
            Component originalComponent,
            Component targetComponent,
            ComponentAssociationMultiplicity targetComponentRelationMultiplicity,
            ComponentAssociation associationType) {
        ComponentRelation relation = new ComponentRelation();
        if (!targetComponent.componentType().isBaseComponent()) {
            throw new IllegalArgumentException(
                    "Target component " + targetComponent.uniqueName() + " is not a base component!");
        }
        if (!SyntheticModuleSupport.isSyntheticModule(originalComponent)) {
            throw new IllegalArgumentException(
                    "Original component " + originalComponent.uniqueName() + " is not a synthetic module!");
        }
        relation.originalComponent = originalComponent;
        relation.targetComponent = targetComponent;
        relation.targetComponentRelationMultiplicity = targetComponentRelationMultiplicity;
        relation.associationType = associationType;
        return relation;
    }

    public ComponentRelation(Component originalCmp, Component targetCmp) {
        this(originalCmp, targetCmp,
                new ComponentAssociationMultiplicity(DiagramConstants.DefaultClassMultiplicities.NONE),
                ComponentAssociation.NONE);
    }

    private void validateRelCmpTypes(Component originalCmp, Component targetCmp) {
        if (!targetCmp.componentType().isBaseComponent()) {
            throw new IllegalArgumentException(
                    "Target component " + targetCmp.uniqueName() + " is not a base component!");
        }
        if (!originalCmp.componentType().isBaseComponent()) {
            throw new IllegalArgumentException(
                    "Original component " + originalCmp.uniqueName() + " is not a base component!");
        }
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

    /**
     * If you want to expose the strength value in JSON, remove @JsonIgnore.
     */
    @JsonProperty("strength")
    public int strength() {
        return this.associationType.strength();
    }

    public Component originalComponent() {
        return originalComponent;
    }

    @JsonProperty("originalComponent")
    private String originalComponentId() {
        return originalComponent.uniqueName();
    }

    @JsonProperty("targetComponent")
    private String targetComponentId() {
        return targetComponent.uniqueName();
    }

    public Component targetComponent() {
        return targetComponent;
    }

    @JsonProperty("targetComponentRelationMultiplicity")
    private String targetComponentRelationMultiplicityVal() {
        return targetComponentRelationMultiplicity.value();
    }

    public ComponentAssociationMultiplicity targetComponentRelationMultiplicity() {
        return targetComponentRelationMultiplicity;
    }

    public ComponentAssociation associationType() {
        return associationType;
    }

    @JsonProperty("associationType")
    private String associationTypeName() {
        return associationType.name();
    }

    @Override
    public int hashCode() {
        String id = originalComponent.uniqueName() + "->" + targetComponent.uniqueName();
        return id.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ComponentRelation)) {
            return false;
        }
        ComponentRelation other = (ComponentRelation) obj;
        return other.originalComponent().equals(this.originalComponent())
                && other.targetComponent().equals(this.targetComponent())
                && other.associationType().name().equals(this.associationType.name());
    }

    @Override
    public int compareTo(ComponentRelation relation) {
        // Higher strength sorts first
        return Integer.compare(relation.strength(), this.strength());
    }
}
