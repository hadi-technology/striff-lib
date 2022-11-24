package com.hadii.striff.extractor;

import com.hadii.clarpse.reference.ComponentReference;
import com.hadii.clarpse.sourcemodel.OOPSourceModelConstants;
import com.hadii.clarpse.sourcemodel.OOPSourceModelConstants.AccessModifiers;
import com.hadii.clarpse.sourcemodel.OOPSourceModelConstants.ComponentType;
import com.hadii.striff.diagram.DiagramComponent;
import com.hadii.striff.diagram.StriffCodeModel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A collection of relationships extracted from a {@link StriffCodeModel}.
 */
public class ExtractedRelationships {

    private final RelationsMap relationMap = new RelationsMap();
    private static final Logger LOGGER = LogManager.getLogger(ExtractedRelationships.class);

    public ExtractedRelationships(final StriffCodeModel sourceCodeModel) {
        final Map<String, DiagramComponent> components = sourceCodeModel.components();
        for (final Map.Entry<String, DiagramComponent> entry : components.entrySet()) {
            final DiagramComponent tempClass = entry.getValue();
            ComponentType cmpType = entry.getValue().componentType();
            if (cmpType.isBaseComponent() || cmpType.isMethodComponent() || cmpType == ComponentType.FIELD) {
                collectComponentRelations(tempClass, components);
            }
        }
    }

    public ExtractedRelationships(ExtractedRelationships... extractedRelationships) {
        for (ExtractedRelationships extractedRelationship : extractedRelationships) {
            for (ComponentRelation componentRelation : extractedRelationship.result().allRels()) {
                this.relationMap.insertRelation(componentRelation);
            }
        }
    }

    public ExtractedRelationships(Set<ComponentRelation> cmpRelations) {
        for (ComponentRelation componentRelation : cmpRelations) {
            this.relationMap.insertRelation(componentRelation);
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
            final DiagramComponent currentBaseComponent = component.parentBaseCmp(components);
            if (currentBaseComponent != null) {
                // Get a list of all the external components referenced
                final Set<ComponentReference> componentReferences = component.references();
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
                            // --> IF INVOCATION SITE IS METHOD or CONSTRUCTOR
                        } else if ((component.componentType() == ComponentType.METHOD
                        || component.componentType() == ComponentType.CONSTRUCTOR)
                                && !currentBaseComponent.uniqueName().equals(targetClass.uniqueName())) {
                            relationAssociation = DiagramConstants.ComponentAssociation.WEAK_ASSOCIATION;
                            externalClassLink = new ComponentRelation(currentBaseComponent, targetClass, relationMultiplicity,
                                    relationAssociation);
                            // --> IF INVOCATION SITE IS METHOD or CONSTRUCTOR
                        } else if ((component.componentType() == ComponentType.METHOD_PARAMETER_COMPONENT
                        || component.componentType() == ComponentType.CONSTRUCTOR_PARAMETER_COMPONENT)
                            && !currentBaseComponent.uniqueName().equals(targetClass.uniqueName())) {
                            relationAssociation = DiagramConstants.ComponentAssociation.ASSOCIATION;
                            externalClassLink = new ComponentRelation(currentBaseComponent, targetClass, relationMultiplicity,
                                                                      relationAssociation);
                        } else {
                            continue;
                        }
                        try {
                            addRelation(externalClassLink);
                        } catch (IllegalArgumentException e) {
                            LOGGER.warn(e);
                        }
                    }
                }
            }
        }
    }

    /**
     * Recursively removes self-referencing implementation and extension relationships.
     */
    private void filterComponentInvocations(Set<ComponentReference> componentReferences,
                                            Map<String, DiagramComponent> components, DiagramComponent unfilteredComponent,
                                            DiagramComponent originalComponent) {
        // Ensure there are no self-referencing implementation relationships..
        if (!unfilteredComponent.references(OOPSourceModelConstants.TypeReferences.IMPLEMENTATION).isEmpty()) {
            for (ComponentReference ref : unfilteredComponent.references(OOPSourceModelConstants.TypeReferences.IMPLEMENTATION)) {
                DiagramComponent invokedComponent = components.get(ref.invokedComponent());
                if (invokedComponent != null && !unfilteredComponent.equals(invokedComponent)) {
                    filterComponentInvocations(componentReferences, components, invokedComponent, originalComponent);
                }
            }
        }
        // Ensure there are no self-referencing extension relationships..
        if (!unfilteredComponent.references(OOPSourceModelConstants.TypeReferences.EXTENSION).isEmpty()) {
            for (ComponentReference ref : unfilteredComponent.references(OOPSourceModelConstants.TypeReferences.EXTENSION)) {
                DiagramComponent invokedComponent = components.get(ref.invokedComponent());
                if (invokedComponent != null && !unfilteredComponent.equals(invokedComponent)) {
                    filterComponentInvocations(componentReferences, components, invokedComponent, originalComponent);
                }
            }
        }
        if (!unfilteredComponent.uniqueName().equals(originalComponent.uniqueName())) {
            componentReferences.removeAll(unfilteredComponent.references());
        }
    }

    /**
     * Registers a new component relation between two components.
     */
    public void addRelation(final ComponentRelation cmpRelation) {
        if (isValidRelation(cmpRelation)) {
            this.relationMap.insertRelation(cmpRelation);
        }
    }

    private boolean isValidRelation(ComponentRelation cmpRelation) {
        return cmpRelation.originalComponent().componentType().isBaseComponent()
            && cmpRelation.targetComponent().componentType().isBaseComponent()
            && !cmpRelation.originalComponent().equals(cmpRelation.targetComponent());
    }

    /**
     * Analyzes the components specialized by the given component.
     */
    private void componentSpecializations(final DiagramComponent component, final Map<String, DiagramComponent> allComponents) {
        final List<ComponentReference> superClasses = component.references(OOPSourceModelConstants.TypeReferences.EXTENSION);
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
     * @param component          Component to be analyzed for relations
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
                .references(OOPSourceModelConstants.TypeReferences.IMPLEMENTATION);
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

    public final RelationsMap result() {
        return this.relationMap;
    }
}
