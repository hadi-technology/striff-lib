package com.clarity.binary.extractor;

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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Returns a list of binary class relationships from a given source code model.
 */
public class BinaryClassRelationshipExtractor<T> implements Serializable {

    /**
     * Finds external class links from all the field, method and method params
     * in the given component.
     */
    private void genAssociations(final Component currentComponent, final Map<String, Component> components,
                                 final List<BinaryClassRelationship> binaryRelationships) {

        // only consider non-base components (eg: methods, fields, etc ..)
        if (!currentComponent.componentType().isBaseComponent()) {
            // get the class the current component's parent base component
            final Component currentBaseComponent = ClarpseUtil.getParentBaseComponent(currentComponent, components);
            if (currentBaseComponent != null) {
                // get a list of all the external class references this current
                // component has..
                final Set<ComponentInvocation> componentInvocations = currentComponent.componentInvocations();
                // remove redundant invocations..
                filterComponentInvocations(componentInvocations, components, currentBaseComponent, currentBaseComponent);
                // loop through list of invocations and create binary class relationships as appropriate..
                for (final ComponentInvocation externalClassTypeRef : componentInvocations) {
                    if (!externalClassTypeRef.empty()) {
                        final String externalClassType = externalClassTypeRef.invokedComponent();
                        if (components.containsKey(externalClassType)) {
                            final Component targetClass = components.get(externalClassType);
                            BinaryClassMultiplicity bCM = null;
                            BinaryClassAssociation bCA;
                            ExternalClassLink externalClassLink;
                            // check the component external types to see if we
                            // have some sort of array
                            for (int i = 0; i < currentComponent
                                    .componentInvocations(ComponentInvocations.DECLARATION).size(); i++) {
                                bCM = new BinaryClassMultiplicity(DefaultClassMultiplicities.NONE);

                            }
                            // create external class link based on calling
                            // component type
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
                                externalClassLink = new ExternalClassLink(currentBaseComponent, targetClass, bCM,
                                        currentComponent.modifiers(), bCA);
                                // --> IF INVOCATION SITE IS METHOD
                            } else if ((currentComponent.componentType() == ComponentType.METHOD)
                                    && !currentBaseComponent.uniqueName().equals(targetClass.uniqueName())) {
                                bCA = BinaryClassAssociation.ASSOCIATION;
                                externalClassLink = new ExternalClassLink(currentBaseComponent, targetClass, bCM,
                                        currentComponent.modifiers(), bCA);
                                // --> IF INVOCATION SITE IS CONSTRUCTOR
                            } else if ((currentComponent.componentType() == ComponentType.CONSTRUCTOR)
                                    && !currentBaseComponent.uniqueName().equals(targetClass.uniqueName())) {
                                bCA = BinaryClassAssociation.ASSOCIATION;
                                externalClassLink = new ExternalClassLink(currentBaseComponent, targetClass, bCM,
                                        currentComponent.modifiers(), bCA);
                                // --> IF INVOCATION SITE IS LOCAL VAR
                            } else if ((currentComponent.componentType() == ComponentType.LOCAL)
                                    && !currentBaseComponent.uniqueName().equals(targetClass.uniqueName())) {
                                bCA = BinaryClassAssociation.WEAK_ASSOCIATION;
                                externalClassLink = new ExternalClassLink(currentBaseComponent, targetClass, bCM,
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

    private static final long serialVersionUID = -8777271960106904851L;

    private void filterComponentInvocations(Set<ComponentInvocation> componentInvocations,
                                            Map<String, Component> components, Component filterComponent, Component originalComponent) {

        if (!filterComponent.componentInvocations(ComponentInvocations.IMPLEMENTATION).isEmpty()) {
            for (ComponentInvocation inv : filterComponent.componentInvocations(ComponentInvocations.IMPLEMENTATION)) {
                Component invokedComponent = components.get(inv.invokedComponent());
                if (invokedComponent != null && !filterComponent.equals(invokedComponent)) {
                    filterComponentInvocations(componentInvocations, components, invokedComponent, originalComponent);
                }
            }
        }

        if (!filterComponent.componentInvocations(ComponentInvocations.EXTENSION).isEmpty()) {
            for (ComponentInvocation inv : filterComponent.componentInvocations(ComponentInvocations.EXTENSION)) {
                Component invokedComponent = components.get(inv.invokedComponent());
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
     * @param externalClassLink external class link from which to generate binary class
     *                          relationships
     */
    private void generateBinaryClassRelationship(final ExternalClassLink externalClassLink,
                                                 final List<BinaryClassRelationship> binaryRelationships) {

        boolean exists = false;
        for (BinaryClassRelationship br : binaryRelationships) {
            if (br.getClassA().equals(externalClassLink.getOrignalClass()) && br.getClassB().equals(externalClassLink.getTargetClass())) {
                exists = true;
                br.resolveExtClassLink(true, externalClassLink);
                break;
            }
            if (br.getClassB().equals(externalClassLink.getOrignalClass()) && br.getClassA().equals(externalClassLink.getTargetClass())) {
                exists = true;
                br.resolveExtClassLink(false, externalClassLink);
                break;
            }
        }
        if (!exists) {
            // there is no existing binary class relationship b/w the two classes, create a new one
            binaryRelationships.add(new BinaryClassRelationship(externalClassLink));
        }
    }

    /**
     * Analyzes the generalization relationships for the given class.
     */
    private void genClassGeneralizations(final Component sourceClass, final Map<String, Component> classes,
                                         final List<BinaryClassRelationship> binaryRelationships) {

        final List<ComponentInvocation> superClasses = sourceClass.componentInvocations(ComponentInvocations.EXTENSION);
        if (!superClasses.isEmpty()) {
            for (final ComponentInvocation superClass : superClasses) {
                if (classes.containsKey(superClass.invokedComponent())) {
                    final Component targetClass = classes.get(superClass.invokedComponent());
                    final ExternalClassLink generalizationExternalClassLink = new ExternalClassLink(sourceClass,
                            targetClass, new BinaryClassMultiplicity(DefaultClassMultiplicities.NONE),
                            sourceClass.modifiers(),
                            BinaryClassAssociation.GENERALISATION);

                    generateBinaryClassRelationship(generalizationExternalClassLink, binaryRelationships);
                }
            }
        }
    }

    /**
     * @param starClass          class to be analyzed for relationships.
     * @param codeBaseComponents list of all classes in the code base.
     */
    private void genClassifierRelationships(final Component starClass, final Map<String, Component> codeBaseComponents,
                                            final List<BinaryClassRelationship> binaryRelationships) {

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
                                      final List<BinaryClassRelationship> binaryRelationships) {

        final List<ComponentInvocation> implementedClasses = sourceClass
                .componentInvocations(ComponentInvocations.IMPLEMENTATION);
        if (!implementedClasses.isEmpty()) {
            for (final ComponentInvocation implementedClass : implementedClasses) {
                if (classes.containsKey(implementedClass.invokedComponent())) {
                    final Component targetClass = classes.get(implementedClass.invokedComponent());
                    final ExternalClassLink realizationExternalClassLink = new ExternalClassLink(sourceClass,
                            targetClass, new BinaryClassMultiplicity(DefaultClassMultiplicities.NONE),
                            sourceClass.modifiers(),
                            BinaryClassAssociation.REALIZATION);
                    generateBinaryClassRelationship(realizationExternalClassLink, binaryRelationships);
                }
            }
        }
    }

    public final List<BinaryClassRelationship> generateBinaryClassRelationships(
            final OOPSourceCodeModel sourceCodeModel) {

        final List<BinaryClassRelationship> binaryRelationships = new ArrayList<>();
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
