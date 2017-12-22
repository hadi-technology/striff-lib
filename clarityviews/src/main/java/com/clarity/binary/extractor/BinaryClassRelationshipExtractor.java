/**
 * Extracts and stores all the binary class relationships from a group of Component Objects.
 */

package com.clarity.binary.extractor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.clarity.ClarpseUtil;
import com.clarity.binary.diagram.DiagramConstants.BinaryClassAssociation;
import com.clarity.binary.diagram.DiagramConstants.DefaultClassMultiplicities;
import com.clarity.invocation.ComponentInvocation;
import com.clarity.sourcemodel.Component;
import com.clarity.sourcemodel.OOPSourceCodeModel;
import com.clarity.sourcemodel.OOPSourceModelConstants;
import com.clarity.sourcemodel.OOPSourceModelConstants.AccessModifiers;
import com.clarity.sourcemodel.OOPSourceModelConstants.ComponentInvocations;
import com.clarity.sourcemodel.OOPSourceModelConstants.ComponentType;

/**
 * Returns a list of binary class relationships from a given source code model.
 */
public class BinaryClassRelationshipExtractor<T> implements Serializable {

    public enum InvocationSiteProperty {

        FIELD, LOCAL, NONE, METHOD_PARAMETER, CONSTRUCTOR_PARAMETER;
    }

    private static final long serialVersionUID = -8777271960106904851L;

    /**
     * Finds external class links from all the field, method and method params
     * in the given component.
     */
    private void genAssociations(final Component currentComponent, final Map<String, Component> components,
            final Map<String, BinaryClassRelationship> binaryRelationships) {

        // only consider non-base components (eg: methods, fields, etc ..)
        if (!currentComponent.componentType().isBaseComponent()) {
            // get the class the current component we are analyzing represents
            final Component currentClass = ClarpseUtil.getParentBaseComponent(currentComponent, components);
            if (currentClass != null) {
                // get a list of all the external class references this current
                // component has..
                final Set<ComponentInvocation> componentInvocations = currentComponent.componentInvocations();
                // remove redundant invocations..
                filterComponentInvocations(componentInvocations, components, currentClass, currentClass);
                for (final ComponentInvocation externalClassTypeRef : componentInvocations) {
                    if (!externalClassTypeRef.empty()) {
                        final String externalClassType = externalClassTypeRef.invokedComponent();
                        if (components.containsKey(externalClassType)) {
                            final Component targetClass = components.get(externalClassType);
                            BinaryClassMultiplicity bCM = null;
                            BinaryClassAssociation bCA = null;
                            ExternalClassLink externalClassLink;
                            // check the component external types to see if we
                            // have some sort of array
                            for (final ComponentInvocation externalTypeRef : currentComponent
                                    .componentInvocations(ComponentInvocations.DECLARATION)) {
                                final String externalType = externalTypeRef.invokedComponent();
                                bCM = new BinaryClassMultiplicity(DefaultClassMultiplicities.NONE);

                            }
                            // create external class link based on calling
                            // component
                            // type
                            // --> IF INVOCATION SITE IS CLASS FIELD:
                            if (currentComponent.componentType() == ComponentType.FIELD) {

                                if (currentComponent.modifiers().contains(
                                        OOPSourceModelConstants.getJavaAccessModifierMap().get(AccessModifiers.PRIVATE))
                                        || currentComponent.modifiers().contains(OOPSourceModelConstants
                                                .getJavaAccessModifierMap().get(AccessModifiers.PROTECTED))) {
                                    bCA = BinaryClassAssociation.COMPOSITION;
                                } else {
                                    bCA = BinaryClassAssociation.AGGREGATION;
                                }
                                externalClassLink = new ExternalClassLink(currentClass, targetClass, bCM,
                                        com.clarity.binary.ClarityUtil.InvocationSiteProperty.FIELD,
                                        currentComponent.modifiers(), bCA);

                                // --> IF INVOCATION SITE IS METHOD
                            } else if ((currentComponent.componentType() == ComponentType.METHOD)
                                    && !currentClass.uniqueName().equals(targetClass.uniqueName())) {

                                bCA = BinaryClassAssociation.ASSOCIATION;
                                externalClassLink = new ExternalClassLink(currentClass, targetClass, bCM,
                                        com.clarity.binary.ClarityUtil.InvocationSiteProperty.METHOD_PARAMETER,
                                        currentComponent.modifiers(), bCA);
                                // --> IF INVOCATION SITE IS CONSTRUCTOR
                            } else if ((currentComponent.componentType() == ComponentType.CONSTRUCTOR)
                                    && !currentClass.uniqueName().equals(targetClass.uniqueName())) {

                                bCA = BinaryClassAssociation.ASSOCIATION;
                                externalClassLink = new ExternalClassLink(currentClass, targetClass, bCM,
                                        com.clarity.binary.ClarityUtil.InvocationSiteProperty.CONSTRUCTOR_PARAMETER,
                                        currentComponent.modifiers(), bCA);
                            } else if ((currentComponent.componentType() == ComponentType.LOCAL)
                                    && !currentClass.uniqueName().equals(targetClass.uniqueName())) {

                                bCA = BinaryClassAssociation.WEAK_ASSOCIATION;
                                externalClassLink = new ExternalClassLink(currentClass, targetClass, bCM,
                                        com.clarity.binary.ClarityUtil.InvocationSiteProperty.LOCAL,
                                        currentComponent.modifiers(), bCA);
                            } else {
                                continue;
                            }
                            generateBinaryClassRelationship(externalClassLink, binaryRelationships);
                        }
                    }
                }
            }
        }
    }

    private void filterComponentInvocations(Set<ComponentInvocation> componentInvocations,
            Map<String, Component> components, Component filterComponent, Component originalComponent) {

        if (!filterComponent.componentInvocations(ComponentInvocations.IMPLEMENTATION).isEmpty()) {
            for (ComponentInvocation inv : filterComponent.componentInvocations(ComponentInvocations.IMPLEMENTATION)) {
                Component invokedComponent = components.get(inv.invokedComponent());
                if (invokedComponent != null) {
                    filterComponentInvocations(componentInvocations, components, invokedComponent, originalComponent);
                }
            }
        }

        if (!filterComponent.componentInvocations(ComponentInvocations.EXTENSION).isEmpty()) {
            for (ComponentInvocation inv : filterComponent.componentInvocations(ComponentInvocations.EXTENSION)) {
                Component invokedComponent = components.get(inv.invokedComponent());
                if (invokedComponent != null) {
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
    private void removeMatchingInvocations(Set<ComponentInvocation> invokedComponentsToBeRemoved,
            Set<ComponentInvocation> externalClassTypeReferences) {

        List<ComponentInvocation> invocationsCopy = new ArrayList<ComponentInvocation>(externalClassTypeReferences);
        for (ComponentInvocation tmpInvocation : invocationsCopy) {
            for (ComponentInvocation toBeRemovedInvocation : invokedComponentsToBeRemoved) {
                if (tmpInvocation.invokedComponent().equals(toBeRemovedInvocation.invokedComponent())) {
                    externalClassTypeReferences.remove(tmpInvocation);
                }
            }
        }
    }

    /**
     * Generates binary class relationships from the given external class link.
     *
     * @param externalClassLink
     *            external class link from which to generate binary class
     *            relationships
     */
    private void generateBinaryClassRelationship(final ExternalClassLink externalClassLink,
            final Map<String, BinaryClassRelationship> binaryRelationships) {

        // determine if we need to merge the incoming External Class Link with
        // an existing binary class relationship
        // between two classes
        final String possibleNameA = BinaryClassRelationship
                .generateRelationshipName(externalClassLink.getOrignalClass(), externalClassLink.getTargetClass());
        final String possibleNameB = BinaryClassRelationship
                .generateRelationshipName(externalClassLink.getTargetClass(), externalClassLink.getOrignalClass());

        if (binaryRelationships.containsKey(possibleNameA)) {
            binaryRelationships.get(possibleNameA).resolveExtClassLink(true, externalClassLink);
        } else if (binaryRelationships.containsKey(possibleNameB)) {
            binaryRelationships.get(possibleNameB).resolveExtClassLink(false, externalClassLink);
        } else {
            // there is no existing binary class relationship b/w the two
            // classes, create a new one
            binaryRelationships.put(BinaryClassRelationship
                    .generateRelationshipName(externalClassLink.getOrignalClass(), externalClassLink.getTargetClass()),
                    new BinaryClassRelationship(externalClassLink));
        }
    }

    /**
     * Analyzes the generalization relationships for the given class.
     */
    private void genClassGeneralizations(final Component sourceClass, final Map<String, Component> classes,
            final Map<String, BinaryClassRelationship> binaryRelationships) {

        final List<ComponentInvocation> superClasses = sourceClass.componentInvocations(ComponentInvocations.EXTENSION);
        if (!superClasses.isEmpty()) {
            for (final ComponentInvocation superClass : superClasses) {
                if (classes.containsKey(superClass.invokedComponent())) {
                    final Component targetClass = classes.get(superClass.invokedComponent());
                    final ExternalClassLink generalizationExternalClassLink = new ExternalClassLink(sourceClass,
                            targetClass, new BinaryClassMultiplicity(DefaultClassMultiplicities.NONE),
                            com.clarity.binary.ClarityUtil.InvocationSiteProperty.NONE, sourceClass.modifiers(),
                            BinaryClassAssociation.GENERALISATION);

                    generateBinaryClassRelationship(generalizationExternalClassLink, binaryRelationships);
                }
            }
        }
    }

    /**
     * @param starClass
     *            class to be analyzed for relationships.
     * @param codeBaseComponents
     *            list of all classes in the code base.
     */
    private void genClassifierRelationships(final Component starClass, final Map<String, Component> codeBaseComponents,
            final Map<String, BinaryClassRelationship> binaryRelationships) {

        // Scan class signature for any classes that have been extended
        genClassGeneralizations(starClass, codeBaseComponents, binaryRelationships);
        // Scan class signature for any classes that have been implemented
        genClassRealizations(starClass, codeBaseComponents, binaryRelationships);
        // Scan class fields for associations
        genAssociations(starClass, codeBaseComponents, binaryRelationships);
    }

    /**
     * Analyzes the realization relationships for the given class.
     */
    private void genClassRealizations(final Component sourceClass, final Map<String, Component> classes,
            final Map<String, BinaryClassRelationship> binaryRelationships) {

        final List<ComponentInvocation> implementedClasses = sourceClass
                .componentInvocations(ComponentInvocations.IMPLEMENTATION);
        if (!implementedClasses.isEmpty()) {
            for (final ComponentInvocation implementedClass : implementedClasses) {
                if (classes.containsKey(implementedClass.invokedComponent())) {
                    final Component targetClass = classes.get(implementedClass.invokedComponent());
                    final ExternalClassLink realizationExternalClassLink = new ExternalClassLink(sourceClass,
                            targetClass, new BinaryClassMultiplicity(DefaultClassMultiplicities.NONE),
                            com.clarity.binary.ClarityUtil.InvocationSiteProperty.NONE, sourceClass.modifiers(),
                            BinaryClassAssociation.REALIZATION);
                    generateBinaryClassRelationship(realizationExternalClassLink, binaryRelationships);
                }
            }
        }
    }

    public final Map<String, BinaryClassRelationship> generateBinaryClassRelationships(
            final OOPSourceCodeModel sourceCodeModel) throws Exception {

        final Map<String, BinaryClassRelationship> binaryRelationships = new ConcurrentHashMap<String, BinaryClassRelationship>();
        final Map<String, com.clarity.sourcemodel.Component> components = sourceCodeModel.getComponents();
        for (final Map.Entry<String, Component> entry : components.entrySet()) {
            final Component tempClass = entry.getValue();
            ComponentType cmpType = entry.getValue().componentType();
            if (cmpType.isBaseComponent() || cmpType.isMethodComponent() || cmpType == ComponentType.FIELD) {
                // generate the binary class relationships..
                genClassifierRelationships(tempClass, components, binaryRelationships);
            }
        }
        return binaryRelationships;
    }
}
