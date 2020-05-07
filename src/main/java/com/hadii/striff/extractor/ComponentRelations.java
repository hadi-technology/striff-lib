package com.hadii.striff.extractor;

import com.hadii.clarpse.reference.ComponentReference;
import com.hadii.clarpse.sourcemodel.OOPSourceModelConstants;
import com.hadii.clarpse.sourcemodel.OOPSourceModelConstants.AccessModifiers;
import com.hadii.clarpse.sourcemodel.OOPSourceModelConstants.ComponentType;
import com.hadii.striff.diagram.DiagramComponent;
import com.hadii.striff.diagram.DiagramConstants;
import com.hadii.striff.diagram.DiagramSourceCodeModel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Returns a list of binary class relationships from a given source code model.
 */
public class ComponentRelations {

    private final Map<DiagramComponent, List<ComponentRelation>> relationMap = new HashMap<>();

    public ComponentRelations(final DiagramSourceCodeModel sourceCodeModel) {
        final Map<String, DiagramComponent> components = sourceCodeModel.components();
        for (final Map.Entry<String, DiagramComponent> entry : components.entrySet()) {
            final DiagramComponent tempClass = entry.getValue();
            ComponentType cmpType = entry.getValue().componentType();
            if (cmpType.isBaseComponent() || cmpType.isMethodComponent() || cmpType == ComponentType.FIELD) {
                collectComponentRelations(tempClass, components);
            }
        }
    }

    public ComponentRelations() {}

    public ComponentRelations(ComponentRelations ... cmpRelations) {
        for (ComponentRelations cmpRelationsObj : cmpRelations) {
            for (ComponentRelation componentRelation : cmpRelationsObj.relations()) {
                this.addRelation(componentRelation);
            }
        }
    }

    /**
     * Finds external class links from all the field, method and method params
     * in the given component.
     */
    private void genAssociations(final DiagramComponent component, final Map<String, DiagramComponent> components) {
        // Only consider non-base components (eg: methods, fields, etc ..)
        if (!component.componentType().isBaseComponent()) {
            // Get the the current component's parent base component
            final DiagramComponent currentBaseComponent = component.parentBaseComponent(components);
            if (currentBaseComponent != null) {
                // Get a list of all the external components referenced
                final Set<ComponentReference> componentReferences = component.componentInvocations();
                // Remove redundant references..
                filterComponentInvocations(componentReferences, components, currentBaseComponent, currentBaseComponent);
                // Loop through list of references and create component relationships as required..
                for (final ComponentReference componentRelationship : componentReferences) {
                    final String componentReferenceName = componentRelationship.invokedComponent();
                    // Important: We only consider component references that refer to components in the given code base!
                    if (components.containsKey(componentReferenceName)) {
                        final DiagramComponent targetClass = components.get(componentReferenceName);
                        ComponentAssociationMultiplicity relationMultiplicity;
                        DiagramConstants.ComponentAssociation relationAssociation;
                        ComponentRelation externalClassLink;
                        // TODO: Set relation multiplicity appropriately based on component references.
                        relationMultiplicity = new ComponentAssociationMultiplicity(DiagramConstants.DefaultClassMultiplicities.NONE);
                        // create external class link based on calling component type
                        // --> IF INVOCATION SITE IS CLASS FIELD:
                        if (component.componentType() == ComponentType.FIELD) {
                            if (component.modifiers().contains(
                                    OOPSourceModelConstants.getJavaAccessModifierMap().get(AccessModifiers.PRIVATE))
                                    || component.modifiers().contains(OOPSourceModelConstants
                                    .getJavaAccessModifierMap().get(AccessModifiers.PROTECTED))) {
                                relationAssociation = DiagramConstants.ComponentAssociation.COMPOSITION;
                            } else {
                                relationAssociation = DiagramConstants.ComponentAssociation.AGGREGATION;
                            }
                            externalClassLink = new ComponentRelation(currentBaseComponent, targetClass, relationMultiplicity,
                                    relationAssociation);
                            // --> IF INVOCATION SITE IS METHOD
                        } else if ((component.componentType() == ComponentType.METHOD)
                                && !currentBaseComponent.uniqueName().equals(targetClass.uniqueName())) {
                            relationAssociation = DiagramConstants.ComponentAssociation.ASSOCIATION;
                            externalClassLink = new ComponentRelation(currentBaseComponent, targetClass, relationMultiplicity,
                                    relationAssociation);
                            // --> IF INVOCATION SITE IS CONSTRUCTOR
                        } else if ((component.componentType() == ComponentType.CONSTRUCTOR)
                                && !currentBaseComponent.uniqueName().equals(targetClass.uniqueName())) {
                            relationAssociation = DiagramConstants.ComponentAssociation.ASSOCIATION;
                            externalClassLink = new ComponentRelation(currentBaseComponent, targetClass, relationMultiplicity,
                                    relationAssociation);
                        } else {
                            continue;
                        }
                        addRelation(externalClassLink);
                    }
                }
            }
        }
    }

    /**
     * Recursively removes self-referencing implementation and extension relationships.
     */
    private void filterComponentInvocations(Set<ComponentReference> componentInvocations,
                                            Map<String, DiagramComponent> components, DiagramComponent filterComponent,
                                            DiagramComponent originalComponent) {
        if (!filterComponent.componentInvocations(OOPSourceModelConstants.TypeReferences.IMPLEMENTATION).isEmpty()) {
            for (ComponentReference ref : filterComponent.componentInvocations(OOPSourceModelConstants.TypeReferences.IMPLEMENTATION)) {
                DiagramComponent invokedComponent = components.get(ref.invokedComponent());
                if (invokedComponent != null && !filterComponent.equals(invokedComponent)) {
                    filterComponentInvocations(componentInvocations, components, invokedComponent, originalComponent);
                }
            }
        }
        if (!filterComponent.componentInvocations(OOPSourceModelConstants.TypeReferences.EXTENSION).isEmpty()) {
            for (ComponentReference ref : filterComponent.componentInvocations(OOPSourceModelConstants.TypeReferences.EXTENSION)) {
                DiagramComponent invokedComponent = components.get(ref.invokedComponent());
                if (invokedComponent != null && !filterComponent.equals(invokedComponent)) {
                    filterComponentInvocations(componentInvocations, components, invokedComponent, originalComponent);
                }
            }
        }
        if (!filterComponent.uniqueName().equals(originalComponent.uniqueName())) {
            removeMatchingInvocations(filterComponent.componentInvocations(), componentInvocations);
        }
    }

    /**
     * Removes all the to-be-removed-invocations from a given list of component
     * invocations.
     */
    private void removeMatchingInvocations(Set<ComponentReference> invokedComponentsToBeRemoved,
                                           Set<ComponentReference> externalClassTypeReferences) {
        List<ComponentReference> invocationsCopy = new ArrayList<>(externalClassTypeReferences);
        for (ComponentReference tmpInvocation : invocationsCopy) {
            for (ComponentReference toBeRemovedInvocation : invokedComponentsToBeRemoved) {
                if (tmpInvocation.invokedComponent().equals(toBeRemovedInvocation.invokedComponent())) {
                    externalClassTypeReferences.remove(tmpInvocation);
                }
            }
        }
    }

    /**
     * Registers a new component relation between two components.
     */
    public void addRelation(final ComponentRelation cmpRelation) {
        if (!cmpRelation.originalComponent().componentType().isBaseComponent() ||
                !cmpRelation.targetComponent().componentType().isBaseComponent()) {
            throw new IllegalArgumentException("Relations must exist between base components only!");
        }
        if (this.relationMap.containsKey(cmpRelation.originalComponent())) {
            if (this.relationMap.get(cmpRelation.originalComponent()).contains(cmpRelation)) {
                // If a relation already exists between the two components of the incoming relation, take the stronger one.
                int relationIndex = this.relationMap.get(cmpRelation.originalComponent()).indexOf(cmpRelation);
                int currRelationStrength =  this.relationMap.get(cmpRelation.originalComponent())
                        .get(relationIndex).associationType().strength();
                if (currRelationStrength < cmpRelation.associationType().strength()) {
                    this.relationMap.get(cmpRelation.originalComponent()).add(relationIndex, cmpRelation);
                }
            }
            this.relationMap.get(cmpRelation.originalComponent()).add(cmpRelation);
        } else {
            List<ComponentRelation> relationships = new ArrayList<>();
            relationships.add(cmpRelation);
            this.relationMap.put(cmpRelation.originalComponent(), relationships);
        }
    }

    /**
     * Analyzes the components specialized by the given component.
     */
    private void componentSpecializations(final DiagramComponent component, final Map<String, DiagramComponent> allComponents) {
        final List<ComponentReference> superClasses = component.componentInvocations(OOPSourceModelConstants.TypeReferences.EXTENSION);
        if (!superClasses.isEmpty()) {
            for (final ComponentReference superClass : superClasses) {
                if (allComponents.containsKey(superClass.invokedComponent())) {
                    final DiagramComponent targetClass = allComponents.get(superClass.invokedComponent());
                    final ComponentRelation specializationRelation = new ComponentRelation(component,
                            targetClass, new ComponentAssociationMultiplicity(DiagramConstants.DefaultClassMultiplicities.NONE),
                            DiagramConstants.ComponentAssociation.SPECIALIZATION);
                    addRelation(specializationRelation);
                }
            }
        }
    }

    /**
     * @param component Component to be analyzed for relations
     * @param codeBaseComponents Map of all components in the code base
     */
    private void collectComponentRelations(final DiagramComponent component, final Map<String, DiagramComponent> codeBaseComponents) {
        // Scan class signature for any classes that have been extended
        componentSpecializations(component, codeBaseComponents);
        // Scan class signature for any classes that have been implemented
        componentRealizations(component, codeBaseComponents);
        // Scan class fields for associations
        genAssociations(component, codeBaseComponents);
    }

    /**
     * Process components realized by the given component.
     */
    private void componentRealizations(final DiagramComponent sourceComponent, final Map<String, DiagramComponent> allComponents) {
        final List<ComponentReference> implementedClasses = sourceComponent
                .componentInvocations(OOPSourceModelConstants.TypeReferences.IMPLEMENTATION);
        if (!implementedClasses.isEmpty()) {
            for (final ComponentReference implementedClass : implementedClasses) {
                if (allComponents.containsKey(implementedClass.invokedComponent())) {
                    final DiagramComponent targetClass = allComponents.get(implementedClass.invokedComponent());
                    final ComponentRelation realizationExternalClassLink = new ComponentRelation(sourceComponent,
                            targetClass, new ComponentAssociationMultiplicity(DiagramConstants.DefaultClassMultiplicities.NONE),
                            DiagramConstants.ComponentAssociation.REALIZATION);
                    addRelation(realizationExternalClassLink);
                }
            }
        }
    }

    public final Collection<ComponentRelation> relations() {
        List<ComponentRelation> relations = new ArrayList<>();
        for (Map.Entry<DiagramComponent, List<ComponentRelation>> entry : this.relationMap.entrySet()) {
            relations.addAll(entry.getValue());
        }
        return relations;
    }

    public final boolean hasRelation(ComponentRelation componentRelation) {
        return this.relationMap.containsKey(componentRelation.originalComponent()) &&
                this.relationMap.get(componentRelation.originalComponent()).contains(componentRelation);
    }

    public final boolean hasRelationsforComponent(DiagramComponent component) {
        return this.relationMap.containsKey(component);
    }

    public final List<ComponentRelation> componentRelations(DiagramComponent component) {
        return this.relationMap.get(component);
    }

    /**
     * Returns a relation going in the opposite direction between the original and target component if it exists.
     * Otherwise, an empty relation is returned.
     */
    public final ComponentRelation reverseRelation(ComponentRelation relation) {
        DiagramComponent componentA = relation.originalComponent();
        DiagramComponent componentB = relation.targetComponent();
        ComponentRelation reverseRelation = new ComponentRelation();
        // Figure out if a component relation in the opposite direction exists
        if (hasRelationsforComponent(componentB)) {
            List<ComponentRelation> targetComponentRelations = componentRelations(componentB);
            targetComponentRelations.removeIf(targetComponentRelation -> !targetComponentRelation.targetComponent().equals(componentA));
            if (targetComponentRelations.size() > 0) {
                reverseRelation = targetComponentRelations.get(0);
            }
        }
        return reverseRelation;
    }
}
