package com.hadi.striff.extractor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hadi.clarpse.reference.ComponentReference;
import com.hadi.clarpse.sourcemodel.Component;
import com.hadi.clarpse.sourcemodel.OOPSourceCodeModel;
import com.hadi.clarpse.sourcemodel.OOPSourceModelConstants;
import com.hadi.clarpse.sourcemodel.OOPSourceModelConstants.AccessModifiers;
import com.hadi.clarpse.sourcemodel.OOPSourceModelConstants.ComponentType;
import com.hadi.striff.annotations.LogExecutionTime;
import com.hadi.striff.diagram.SyntheticModuleSupport;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Extracts and manages relationships from an {@link OOPSourceCodeModel}.
 *
 * <p>This class processes all components in a source code model to identify
 * relationships such as:</p>
 * <ul>
 *   <li><strong>Specializations</strong> - inheritance relationships (extends)</li>
 *   <li><strong>Realizations</strong> - interface implementations (implements)</li>
 *   <li><strong>Associations</strong> - field references, method invocations, parameters</li>
 * </ul>
 *
 * <p>The extraction process:</p>
 * <ol>
 *   <li>Filters for relevant components (base classes, methods, fields)</li>
 *   <li>Analyzes specializations and realizations for each component</li>
 *   <li>Extracts associations based on component type and visibility</li>
 *   <li>Creates synthetic modules for module-level components (Python/TypeScript)</li>
 * </ol>
 *
 * <p><strong>Performance note:</strong> Relationship extraction is the most
 * expensive operation in the Striff pipeline (~45% of processing time for a
 * 1000-file codebase). The {@link RelationsMap} result is typically used
 * directly without defensive copying to avoid additional overhead.</p>
 */

public class ExtractedRelationships {

    private final RelationsMap relationMap = new RelationsMap();
    private static final Logger LOGGER = LoggerFactory.getLogger(ExtractedRelationships.class);

    /**
     * Extracts all relationships from the given source code model.
     *
     * <p>This constructor processes all relevant components and builds a complete
     * relation map. For large codebases, this is an expensive operation and should
     * be called once per model, with results cached when possible.</p>
     *
     * @param sourceCodeModel the model to extract relationships from
     */
    @LogExecutionTime
    public ExtractedRelationships(final OOPSourceCodeModel sourceCodeModel) {
        sourceCodeModel.components()
                .filter(this::isRelevantComponent)
                .forEach(component -> processComponentRelations(component, sourceCodeModel));

        // Create synthetic modules and fold module-level relations
        createSyntheticModulesAndRelations(sourceCodeModel);
    }

    /**
     * Creates synthetic modules for module-level components and folds their relations.
     */
    private void createSyntheticModulesAndRelations(OOPSourceCodeModel sourceCodeModel) {
        Set<Component> moduleLevelComponents = sourceCodeModel.components()
                .filter(SyntheticModuleSupport::isModuleLevelComponent)
                .collect(java.util.stream.Collectors.toSet());

        if (moduleLevelComponents.isEmpty()) {
            return;
        }

        SyntheticModuleSupport.syntheticComponentsByModule(sourceCodeModel).forEach((moduleKey, synthetic) -> {
            // Create relations from module-level components to the synthetic module
            Set<Component> modulesComps = sourceCodeModel.components()
                    .filter(SyntheticModuleSupport::isModuleLevelComponent)
                    .filter(cmp -> moduleKey.equals(cmp.module()))
                    .collect(java.util.stream.Collectors.toSet());

            for (Component moduleLevelCmp : modulesComps) {
                Set<ComponentReference> references = new LinkedHashSet<>(moduleLevelCmp.internalDependencies());

                for (ComponentReference ref : references) {
                    if (!sourceCodeModel.containsComponent(ref.invokedComponent())) {
                        continue;
                    }

                    Component target = sourceCodeModel.getComponent(ref.invokedComponent()).orElse(null);
                    if (target == null) {
                        continue;
                    }

                    // If target is not a base component, try to get its parent
                    if (!target.componentType().isBaseComponent()) {
                        try {
                            target = sourceCodeModel.parentBaseCmp(target.uniqueName());
                        } catch (IllegalArgumentException e) {
                            LOGGER.debug("No parent base component for reference target: {}", ref.invokedComponent());
                            continue;
                        }
                    }

                    if (target == null || target.equals(synthetic)) {
                        continue;
                    }

                    // Determine the association type
                    DiagramConstants.ComponentAssociation associationType;
                    if (moduleLevelCmp.componentType() == ComponentType.FIELD
                            || moduleLevelCmp.componentType() == ComponentType.MODULE_FIELD) {
                        associationType = DiagramConstants.ComponentAssociation.COMPOSITION;
                    } else {
                        associationType = DiagramConstants.ComponentAssociation.WEAK_ASSOCIATION;
                    }

                    // Create the relation from synthetic module to target
                    ComponentRelation relation = ComponentRelation.forSyntheticModule(
                            synthetic,
                            target,
                            new ComponentAssociationMultiplicity(DiagramConstants.DefaultClassMultiplicities.NONE),
                            associationType);
                    this.relationMap.insertRelation(relation);
                }
            }
        });
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
        Stream.concat(component.internalDependencies().stream(), component.externalDependencies().stream())
                .filter(ref -> OOPSourceModelConstants.TypeReferences.EXTENSION
                        .getMatchingClass().isAssignableFrom(ref.getClass()))
                .map(ref -> resolveTargetBaseComponent(ref, model))
                .filter(target -> target != null)
                .map(target -> createRelation(component, target, DiagramConstants.ComponentAssociation.SPECIALIZATION))
                .forEach(this::addRelationSafely);
    }

    /**
     * Analyzes realization relationships (e.g., implementation).
     */
    private void analyzeRealizations(final Component component, final OOPSourceCodeModel model) {
        Stream.concat(component.internalDependencies().stream(), component.externalDependencies().stream())
                .filter(ref -> OOPSourceModelConstants.TypeReferences.IMPLEMENTATION
                        .getMatchingClass().isAssignableFrom(ref.getClass()))
                .map(ref -> resolveTargetBaseComponent(ref, model))
                .filter(target -> target != null)
                .map(target -> createRelation(component, target, DiagramConstants.ComponentAssociation.REALIZATION))
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

        // Skip module-level components - their relations will be created by SyntheticModuleAugmenter
        if (SyntheticModuleSupport.isModuleLevelComponent(component)) {
            return;
        }

        Component baseComponent = findParentBaseComponent(component, model);
        if (baseComponent == null) {
            return;
        }

        Set<ComponentReference> references = Stream.concat(
                component.internalDependencies().stream(),
                component.externalDependencies().stream())
                .collect(Collectors.toCollection(LinkedHashSet::new));
        removeRedundantReferences(references, model, baseComponent);

        references.stream()
                .map(ref -> resolveTargetBaseComponent(ref, model))
                .filter(target -> target != null)
                .filter(target -> !target.equals(baseComponent))
                .map(target -> createAssociation(component, baseComponent, target))
                .filter(this::isValidRelation)
                .forEach(this::addRelationSafely);
    }

    private Component resolveTargetBaseComponent(ComponentReference ref, OOPSourceCodeModel model) {
        if (!model.containsComponent(ref.invokedComponent())) {
            return null;
        }
        Component target = model.getComponent(ref.invokedComponent()).orElse(null);
        if (target == null) {
            return null;
        }
        if (target.componentType().isBaseComponent()) {
            return target;
        }
        try {
            return model.parentBaseCmp(target.uniqueName());
        } catch (IllegalArgumentException e) {
            LOGGER.warn("No parent base component found for reference target: {}", target.uniqueName());
            return null;
        }
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
                .contains(OOPSourceModelConstants.getAccessModifierMap().get(AccessModifiers.PRIVATE))
                || component.modifiers()
                        .contains(OOPSourceModelConstants.getAccessModifierMap().get(AccessModifiers.PROTECTED))) {
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
     * Validates if a relation is valid.
     * - Target component must be a base component
     * - Not self-referencing
     * - Original component can be a base component OR a module-level component (FUNCTION or MODULE_FIELD)
     */
    private boolean isValidRelation(ComponentRelation relation) {
        boolean originalComponentIsValid = relation.originalComponent().componentType().isBaseComponent()
                || SyntheticModuleSupport.isModuleLevelComponent(relation.originalComponent());
        return originalComponentIsValid
                && relation.targetComponent().componentType().isBaseComponent()
                && !relation.originalComponent().equals(relation.targetComponent());
    }

    /**
     * Returns the extracted relationships as an immutable RelationsMap.
     *
     * <p>The returned map contains all relationships discovered during extraction,
     * organized by source component. This map can be efficiently filtered using
     * {@link RelationsMap#filteredRelations(Set)} to obtain subsets of relations.</p>
     *
     * @return the complete relation map for the source model
     */
    public RelationsMap result() {
        return this.relationMap;
    }
}
