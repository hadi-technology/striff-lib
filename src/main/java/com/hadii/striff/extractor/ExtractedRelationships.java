package com.hadii.striff.extractor;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.hadii.clarpse.reference.ComponentReference;
import com.hadii.clarpse.sourcemodel.Component;
import com.hadii.clarpse.sourcemodel.OOPSourceCodeModel;
import com.hadii.clarpse.sourcemodel.OOPSourceModelConstants;
import com.hadii.clarpse.sourcemodel.OOPSourceModelConstants.AccessModifiers;
import com.hadii.clarpse.sourcemodel.OOPSourceModelConstants.ComponentType;
import com.hadii.striff.annotations.LogExecutionTime;

import java.util.Set;

/**
 * Extracts and manages relationships from an {@link OOPSourceCodeModel}.
 */

public class ExtractedRelationships {

    private final RelationsMap relationMap = new RelationsMap();
    private static final Logger LOGGER = LogManager.getLogger(ExtractedRelationships.class);

    @LogExecutionTime
    public ExtractedRelationships(final OOPSourceCodeModel sourceCodeModel) {
        sourceCodeModel.components()
                .filter(this::isRelevantComponent)
                .forEach(component -> processComponentRelations(component, sourceCodeModel));
    }

    /**
     * Filters relevant components (base, methods, fields) for relation extraction.
     */
    private boolean isRelevantComponent(Component component) {
        ComponentType type = component.componentType();
        return type.isBaseComponent() || type.isMethodComponent() || type == ComponentType.FIELD;
    }

    /**
     * Processes relationships for the given component.
     */
    private void processComponentRelations(final Component component, final OOPSourceCodeModel model) {
        analyzeSpecializations(component, model);
        analyzeRealizations(component, model);
        extractAssociations(component, model);
    }

    /**
     * Analyzes specialization relationships (e.g., inheritance).
     */
    private void analyzeSpecializations(final Component component, final OOPSourceCodeModel model) {
        component.references(OOPSourceModelConstants.TypeReferences.EXTENSION).stream()
                .filter(ref -> model.containsComponent(ref.invokedComponent()))
                .map(ref -> createRelation(component, model.getComponent(ref.invokedComponent()).get(),
                        DiagramConstants.ComponentAssociation.SPECIALIZATION))
                .forEach(this::addRelationSafely);
    }

    /**
     * Analyzes realization relationships (e.g., implementation).
     */
    private void analyzeRealizations(final Component component, final OOPSourceCodeModel model) {
        component.references(OOPSourceModelConstants.TypeReferences.IMPLEMENTATION).stream()
                .filter(ref -> model.containsComponent(ref.invokedComponent()))
                .map(ref -> createRelation(component, model.getComponent(ref.invokedComponent()).get(),
                        DiagramConstants.ComponentAssociation.REALIZATION))
                .forEach(this::addRelationSafely);
    }

    /**
     * Extracts associations (e.g., field, method, parameter associations) for the
     * given component.
     */
    private void extractAssociations(final Component component, OOPSourceCodeModel model) {
        if (component.componentType().isBaseComponent()) {
            return;
        }

        Component baseComponent = findParentBaseComponent(component, model);
        if (baseComponent == null) {
            return;
        }

        Set<ComponentReference> references = component.references();
        removeRedundantReferences(references, model, baseComponent);

        references.stream()
                .filter(ref -> model.containsComponent(ref.invokedComponent()))
                .map(ref -> createAssociation(component, baseComponent,
                        model.getComponent(ref.invokedComponent()).get()))
                .filter(this::isValidRelation)
                .forEach(this::addRelationSafely);
    }

    /**
     * Finds the parent base component of the given component.
     */
    private Component findParentBaseComponent(Component component, OOPSourceCodeModel model) {
        try {
            return model.parentBaseCmp(component.uniqueName());
        } catch (IllegalArgumentException e) {
            LOGGER.warn("No parent component found for component: {}", component.uniqueName());
            return null;
        }
    }

    /**
     * Removes redundant self-referencing invocations.
     */
    private void removeRedundantReferences(Set<ComponentReference> references, OOPSourceCodeModel model,
            Component baseComponent) {
        references.removeIf(ref -> isSelfReferencing(ref, baseComponent, model));
    }

    private boolean isSelfReferencing(ComponentReference ref, Component baseComponent, OOPSourceCodeModel model) {
        return ref.invokedComponent().equals(baseComponent.uniqueName())
                || !model.containsComponent(ref.invokedComponent());
    }

    /**
     * Creates an association between components based on their relationship type.
     */
    private ComponentRelation createAssociation(Component component, Component baseComponent,
            Component targetComponent) {
        DiagramConstants.ComponentAssociation associationType = determineAssociationType(component, targetComponent);
        return new ComponentRelation(baseComponent, targetComponent,
                new ComponentAssociationMultiplicity(DiagramConstants.DefaultClassMultiplicities.NONE),
                associationType);
    }

    private DiagramConstants.ComponentAssociation determineAssociationType(Component component,
            Component targetComponent) {
        switch (component.componentType()) {
            case FIELD:
                return determineFieldAssociation(component);
            case METHOD:
            case CONSTRUCTOR:
                return DiagramConstants.ComponentAssociation.WEAK_ASSOCIATION;
            case METHOD_PARAMETER_COMPONENT:
            case CONSTRUCTOR_PARAMETER_COMPONENT:
                return DiagramConstants.ComponentAssociation.ASSOCIATION;
            default:
                return null;
        }
    }

    private DiagramConstants.ComponentAssociation determineFieldAssociation(Component component) {
        if (component.modifiers()
                .contains(OOPSourceModelConstants.getJavaAccessModifierMap().get(AccessModifiers.PRIVATE))
                || component.modifiers()
                        .contains(OOPSourceModelConstants.getJavaAccessModifierMap().get(AccessModifiers.PROTECTED))) {
            return DiagramConstants.ComponentAssociation.COMPOSITION;
        } else {
            return DiagramConstants.ComponentAssociation.AGGREGATION;
        }
    }

    /**
     * Adds a relation safely, logging any issues.
     */
    private void addRelationSafely(ComponentRelation relation) {
        try {
            addRelation(relation);
        } catch (IllegalArgumentException e) {
            LOGGER.warn("Failed to add relation: {}", e.getMessage());
        }
    }

    /**
     * Creates a simple component relation.
     */
    private ComponentRelation createRelation(Component source, Component target,
            DiagramConstants.ComponentAssociation associationType) {
        return new ComponentRelation(source, target,
                new ComponentAssociationMultiplicity(DiagramConstants.DefaultClassMultiplicities.NONE),
                associationType);
    }

    /**
     * Adds a valid relation to the relations map.
     */
    public void addRelation(final ComponentRelation relation) {
        if (isValidRelation(relation)) {
            this.relationMap.insertRelation(relation);
        }
    }

    /**
     * Validates if a relation is between two base components and not
     * self-referencing.
     */
    private boolean isValidRelation(ComponentRelation relation) {
        return relation.originalComponent().componentType().isBaseComponent()
                && relation.targetComponent().componentType().isBaseComponent()
                && !relation.originalComponent().equals(relation.targetComponent());
    }

    /**
     * Retrieves the extracted relationships as a RelationsMap.
     */
    public RelationsMap result() {
        return this.relationMap;
    }
}
