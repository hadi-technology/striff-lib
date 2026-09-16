package com.hadi.striff.extractor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hadi.clarpse.reference.AnnotationReference;
import com.hadi.clarpse.reference.ComponentReference;
import com.hadi.clarpse.sourcemodel.Component;
import com.hadi.clarpse.sourcemodel.OOPSourceCodeModel;
import com.hadi.clarpse.sourcemodel.OOPSourceModelConstants;
import com.hadi.clarpse.sourcemodel.OOPSourceModelConstants.AccessModifiers;
import com.hadi.clarpse.sourcemodel.OOPSourceModelConstants.ComponentType;
import com.hadi.striff.annotations.LogExecutionTime;
import com.hadi.striff.diagram.SyntheticModuleSupport;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CancellationException;
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
     * The synthetic module standing in for each module key in the model.
     *
     * <p>Built once, before any relation is extracted, because a reference to a module-level
     * function or field has to resolve to the module drawn in its place, and that resolution
     * happens on the hottest read path in the pipeline.
     */
    private final Map<String, Component> syntheticModules;

    /**
     * The synthetic module each module-level function or field belongs to, indexed by every name a
     * reference can use to name that member.
     *
     * <p>A module-level function is held under a unique name carrying its signature, such as
     * {@code src.a.helper() : Any}, while a reference to it names only {@code src.a.helper}, so a
     * lookup by the referenced name alone finds nothing and the reference was discarded.
     */
    private final Map<String, Component> modulesByMemberReference;

    /**
     * How many components are processed between interrupt checks during extraction.
     *
     * <p>Relationship extraction is the single most expensive phase in the pipeline and is
     * single-threaded and CPU-bound. A caller that enforces a time budget interrupts this thread
     * when the budget expires, and without a cooperative checkpoint the extraction runs to
     * completion regardless. This mirrors the cooperative-cancellation checkpoint that
     * {@code OOPSourceCodeModel.merge} performs in clarpse: checking every few hundred components
     * keeps cancellation responsive within milliseconds while the overhead is unmeasurable, and the
     * checkpoint is completely inert on any run that is never interrupted.
     */
    private static final int INTERRUPT_CHECK_INTERVAL = 512;

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
        this.syntheticModules = SyntheticModuleSupport.syntheticComponentsByModule(sourceCodeModel);
        this.modulesByMemberReference = indexModulesByMemberReference(sourceCodeModel, this.syntheticModules);
        final List<Component> relevantComponents = sourceCodeModel.components()
                .filter(this::isRelevantComponent)
                .collect(Collectors.toList());

        // Upfront as well as periodic, so an extraction that begins already-cancelled stops before
        // it touches the first component rather than after the first batch.
        throwIfInterrupted();
        int sinceCheck = 0;
        for (final Component component : relevantComponents) {
            if (++sinceCheck >= INTERRUPT_CHECK_INTERVAL) {
                sinceCheck = 0;
                throwIfInterrupted();
            }
            processComponentRelations(component, sourceCodeModel);
        }

        // Create synthetic modules and fold module-level relations
        createSyntheticModulesAndRelations(sourceCodeModel);
    }

    /**
     * Aborts extraction when the calling thread has been interrupted.
     *
     * <p>Cooperative cancellation: a caller that enforces a time budget interrupts this thread, and
     * relationship extraction is one of the long CPU-bound phases that would otherwise ignore the
     * request. Re-asserts the interrupt flag before throwing so callers can still observe it, then throws an
     * unchecked {@link CancellationException} that unwinds out of the constructor (and, in turn, out
     * of {@code CodeDiff} / {@code StriffOperation}) so the caller can convert it to a timeout.
     */
    private static void throwIfInterrupted() {
        if (Thread.currentThread().isInterrupted()) {
            Thread.currentThread().interrupt();
            throw new CancellationException("Analysis interrupted during relationship extraction.");
        }
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

        this.syntheticModules.forEach((moduleKey, synthetic) -> {
            // Create relations from module-level components to the synthetic module
            Set<Component> modulesComps = sourceCodeModel.components()
                    .filter(SyntheticModuleSupport::isModuleLevelComponent)
                    .filter(cmp -> moduleKey.equals(SyntheticModuleSupport.moduleKey(cmp)))
                    .collect(java.util.stream.Collectors.toSet());

            int sinceCheck = 0;
            for (Component moduleLevelCmp : modulesComps) {
                for (ComponentReference ref : allReferences(moduleLevelCmp)) {
                    if (++sinceCheck >= INTERRUPT_CHECK_INTERVAL) {
                        sinceCheck = 0;
                        throwIfInterrupted();
                    }
                    // Annotations/decorators are captured on the model for doc-fact consumers but
                    // are not structural diagram dependencies, so they never become module edges.
                    if (ref instanceof AnnotationReference) {
                        continue;
                    }
                    Component target = sourceCodeModel.component(ref.invokedComponent()).orElse(null);
                    if (target == null) {
                        // Not resolvable by name: a member of another module, or a type that is
                        // genuinely outside the model, which stays undrawn.
                        target = this.modulesByMemberReference.get(ref.invokedComponent());
                        if (target == null) {
                            continue;
                        }
                    }

                    // If the target is not a base component, resolve it to the component the
                    // diagram draws in its place: its own module when the target is itself
                    // module-level, otherwise the class that owns it.
                    if (!target.componentType().isBaseComponent()) {
                        Component targetModule = syntheticModuleOf(target);
                        if (targetModule != null) {
                            target = targetModule;
                        } else {
                            try {
                                target = sourceCodeModel.parentBaseComponent(target.uniqueName());
                            } catch (IllegalArgumentException e) {
                                LOGGER.debug("No parent base component for reference target: {}",
                                        ref.invokedComponent());
                                continue;
                            }
                        }
                    }

                    if (target == null || target.uniqueName().equals(synthetic.uniqueName())) {
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
                // Annotations/decorators are captured on the model for doc-fact consumers but are
                // not structural diagram dependencies, so they must not become association edges.
                .filter(ref -> !(ref instanceof AnnotationReference))
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

    /**
     * Resolves a reference to the base component it lands in.
     *
     * <p>Reads the model's own components rather than copies of them. This is the single hottest read
     * path in the pipeline -- it runs once per reference in the model, and the model is extracted from
     * three times in a two-revision analysis -- and {@code copyOfComponent} deep-copies a component's
     * imports, children and every one of its references in order to answer a question about its type.
     * Nothing here mutates what it is given, and the relation the caller builds only ever reads.
     */
    private Component resolveTargetBaseComponent(ComponentReference ref, OOPSourceCodeModel model) {
        Component target = model.component(ref.invokedComponent()).orElse(null);
        if (target == null) {
            // Not resolvable by name: a module-level member, or a type outside the model entirely.
            return this.modulesByMemberReference.get(ref.invokedComponent());
        }
        if (target.componentType().isBaseComponent()) {
            return target;
        }
        Component targetModule = syntheticModuleOf(target);
        if (targetModule != null) {
            return targetModule;
        }
        try {
            return model.parentBaseComponent(target.uniqueName());
        } catch (IllegalArgumentException e) {
            LOGGER.warn("No parent base component found for reference target: {}", target.uniqueName());
            return null;
        }
    }

    /**
     * Returns the synthetic module a module-level function or field belongs to.
     *
     * <p>A module-level component has no parent class, so a reference to one has no base component
     * to land on and was discarded. Its module is what the diagram draws in its place -- exactly as
     * it does for the module-level component itself -- so that is what such a reference resolves to,
     * and the relation is drawn between the two modules.
     *
     * <p>A reference to a type that is genuinely absent from the model, such as a type owned by an
     * external library, is not module-level and still resolves to nothing.
     *
     * @param target the referenced component
     * @return the synthetic module holding the target, or null if the target is not module-level
     */
    private Component syntheticModuleOf(Component target) {
        if (!SyntheticModuleSupport.isModuleLevelComponent(target)) {
            return null;
        }
        String module = target.module();
        if (module == null || module.trim().isEmpty()) {
            return null;
        }
        return this.syntheticModules.get(SyntheticModuleSupport.moduleKey(target));
    }

    /**
     * Indexes each module-level member's own module under every name a reference can use for it:
     * the member's unique name, and that name without the signature it carries.
     */
    private static Map<String, Component> indexModulesByMemberReference(
            final OOPSourceCodeModel model, final Map<String, Component> syntheticModules) {
        Map<String, Component> index = new HashMap<>();
        model.components()
                .filter(SyntheticModuleSupport::isModuleLevelComponent)
                .filter(member -> member.module() != null && !member.module().trim().isEmpty())
                .forEach(member -> {
                    Component module = syntheticModules.get(SyntheticModuleSupport.moduleKey(member));
                    if (module == null) {
                        return;
                    }
                    index.put(member.uniqueName(), module);
                    index.put(signatureFreeName(member.uniqueName()), module);
                });
        return index;
    }

    /**
     * Strips the signature a function's unique name carries, so {@code src.a.helper() : Any}
     * becomes {@code src.a.helper}, which is how a reference to it is recorded.
     */
    private static String signatureFreeName(final String uniqueName) {
        int signatureStart = uniqueName.indexOf('(');
        if (signatureStart < 0) {
            return uniqueName;
        }
        return uniqueName.substring(0, signatureStart).trim();
    }

    /**
     * Every structural reference a component makes.
     *
     * <p>A reference from one module to a member of another is recorded as an external dependency,
     * so reading only the internal ones leaves module-to-module edges undrawn.
     */
    private static Set<ComponentReference> allReferences(final Component component) {
        return Stream.concat(component.internalDependencies().stream(),
                        component.externalDependencies().stream())
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    /**
     * Finds the parent base component of the given component.
     */
    private Component findParentBaseComponent(Component component, OOPSourceCodeModel model) {
        try {
            return model.parentBaseComponent(component.uniqueName());
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
        if (ref.invokedComponent().equals(baseComponent.uniqueName())) {
            return true;
        }
        // A reference naming a module-level member is not absent from the model: it names a member
        // under the name a reference uses for it, rather than the one the member is held under.
        return !model.containsComponent(ref.invokedComponent())
                && !this.modulesByMemberReference.containsKey(ref.invokedComponent());
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
