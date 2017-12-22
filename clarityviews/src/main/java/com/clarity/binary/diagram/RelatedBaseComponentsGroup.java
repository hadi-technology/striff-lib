package com.clarity.binary.diagram;

import com.clarity.binary.diagram.DiagramConstants.BinaryClassAssociation;
import com.clarity.binary.extractor.BinaryClassRelationship;
import com.clarity.invocation.ComponentInvocation;
import com.clarity.sourcemodel.Component;
import com.clarity.sourcemodel.OOPSourceModelConstants;
import net.sf.oval.constraint.NotEmpty;
import net.sf.oval.constraint.NotNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Represents a group of base components that are related to each other.
 */
public class RelatedBaseComponentsGroup {

    @NotNull
    private Map<String, Component> allComponents;

    @NotNull
    private Map<String, BinaryClassRelationship> allRelationships;

    @NotNull
    @NotEmpty
    private Set<String> mainComponents;

    /**
     * @param allComponents    All the components to be considered.
     * @param allRelationships All the binary relationships between the components.
     * @param mainComponent    The component all other components in the group must have a
     *                         relation with.
     */
    public RelatedBaseComponentsGroup(final Map<String, Component> allComponents,
                                      final Map<String, BinaryClassRelationship> allRelationships, final Component mainComponent) {
        this.allComponents = allComponents;
        this.allRelationships = allRelationships;
        this.mainComponents = new HashSet<String>();
        this.mainComponents.add(mainComponent.uniqueName());

    }

    /**
     * @param allComponents       All the components to be considered.
     * @param allRelationships    All the binary relationships between the components to be
     *                            considered.
     * @param addedBaseComponents A list of components that are basis of and must be included in
     *                            the result set.
     */
    public RelatedBaseComponentsGroup(final Map<String, Component> allComponents,
                                      final Map<String, BinaryClassRelationship> allRelationships, final Set<String> addedBaseComponents) {
        this.allComponents = allComponents;
        this.allRelationships = allRelationships;
        this.mainComponents = addedBaseComponents;
        List<Component> newComponents = new ArrayList<Component>();
        for (String s : addedBaseComponents) {
            newComponents.add(allComponents.get(s));
        }
    }

    /**
     * Creates a list of components who are closely related to the given
     * components.
     */
    public Set<Component> components() {

        final List<Component> overallRelatedGroup = new ArrayList<>();
        for (String cmpName : mainComponents) {
            Component cmp = allComponents.get(cmpName);

            String potentialShortenedCmpName = cmpName;
            if (cmpName.contains(".")) {
                potentialShortenedCmpName = cmpName.substring(cmpName.lastIndexOf(".") + 1);
            }
            final String shortName = potentialShortenedCmpName;
            // create a filtered map for all relationships relevant to the
            // current loop Component.
            Map<String, BinaryClassRelationship> relevantBinaryRelationships = allRelationships.entrySet().stream()
                    .filter(map -> map.getKey().contains(shortName))
                    .collect(Collectors.toMap(p -> p.getKey(), p -> p.getValue()));

            final List<Component> componentRelatedGroup = new ArrayList<>();
            componentRelatedGroup.add(cmp);

            /**
             * Filter stage 1: form a super class hierarchy chain of the key
             * component.
             */
            final List<Component> tmpSuperComponentRelatedGroup = new ArrayList<Component>();
            tmpSuperComponentRelatedGroup.add(cmp);
            int superClassMatches = 0;
            for (int j = 0; j < tmpSuperComponentRelatedGroup.size(); j++) {
                for (final Map.Entry<String, BinaryClassRelationship> entry : relevantBinaryRelationships.entrySet()) {
                    /**
                     * for the component represented by position j, only collect
                     * a maximum of MAX_MATCHES_PER_COMPONENT components related
                     * to j (see below). This is because we want to keep diagram
                     * sizes to minimum.
                     */
                    if (superClassMatches >= 2) {
                        break;
                    }

                    final BinaryClassRelationship bCR = entry.getValue();

                    if (bCR.getClassA().uniqueName().equals(tmpSuperComponentRelatedGroup.get(j).uniqueName())
                            && (bCR.getaSideAssociation() == BinaryClassAssociation.GENERALISATION
                            || bCR.getaSideAssociation() == BinaryClassAssociation.REALIZATION)) {
                        if (!tmpSuperComponentRelatedGroup.contains(bCR.getClassB())) {
                            // super class, place at beginning of list.
                            tmpSuperComponentRelatedGroup.add(bCR.getClassB());
                            superClassMatches++;
                        }
                    }

                    if (bCR.getClassB().uniqueName().equals(tmpSuperComponentRelatedGroup.get(j).uniqueName())
                            && (bCR.getbSideAssociation() == BinaryClassAssociation.GENERALISATION
                            || bCR.getbSideAssociation() == BinaryClassAssociation.REALIZATION)) {
                        if (!tmpSuperComponentRelatedGroup.contains(bCR.getClassA())) {
                            // super class, place at beginning of list.
                            tmpSuperComponentRelatedGroup.add(bCR.getClassA());
                            superClassMatches++;
                        }
                    }

                }
            }

            /**
             * Filter stage 2: form a sub class hierarchy chain of the key
             * component.
             */
            final List<Component> tmpSubComponentRelatedGroup = new ArrayList<Component>();
            tmpSubComponentRelatedGroup.add(cmp);
            int subClassMatches = 0;
            for (int j = 0; j < tmpSubComponentRelatedGroup.size(); j++) {
                for (final Map.Entry<String, BinaryClassRelationship> entry : relevantBinaryRelationships.entrySet()) {
                    if (subClassMatches >= 2) {
                        break;
                    }

                    final BinaryClassRelationship bCR = entry.getValue();

                    if (bCR.getClassA().uniqueName().equals(tmpSubComponentRelatedGroup.get(j).uniqueName())
                            && (bCR.getbSideAssociation() == BinaryClassAssociation.GENERALISATION
                            || bCR.getbSideAssociation() == BinaryClassAssociation.REALIZATION)) {
                        if (!tmpSubComponentRelatedGroup.contains(bCR.getClassB())) {
                            tmpSubComponentRelatedGroup.add(bCR.getClassB());
                            subClassMatches++;
                        }
                    }

                    if (bCR.getClassB().uniqueName().equals(tmpSubComponentRelatedGroup.get(j).uniqueName())
                            && (bCR.getaSideAssociation() == BinaryClassAssociation.GENERALISATION
                            || bCR.getaSideAssociation() == BinaryClassAssociation.REALIZATION)) {
                        if (!tmpSubComponentRelatedGroup.contains(bCR.getClassA())) {
                            tmpSubComponentRelatedGroup.add(bCR.getClassA());
                            subClassMatches++;
                        }
                    }
                }
            }

            /**
             * combine the super and sub component lists so that the super
             * components are at the beginning and the sub components are at the
             * end. Note the first component of the temporary lists are the main
             * component.
             */
            componentRelatedGroup.addAll(0,
                    tmpSuperComponentRelatedGroup.subList(1, tmpSuperComponentRelatedGroup.size()));
            componentRelatedGroup.addAll(tmpSubComponentRelatedGroup.subList(1, tmpSubComponentRelatedGroup.size()));

            /**
             * Filter stage 3: Any components mentioned by documentation should be included.
             */

            for (ComponentInvocation docMention : cmp.componentInvocations(OOPSourceModelConstants.ComponentInvocations.DOC_MENTION)) {
                Component mentionedComponent = allComponents.get(docMention.invokedComponent());
                if (mentionedComponent != null) {
                    componentRelatedGroup.add(mentionedComponent);
                }
            }

            /**
             * Filter stage 4: Start with the beginning of the current result
             * list and look for composition/aggregation relationships.
             */
            int compositionMatches = 0;
            for (int j = 0; j < componentRelatedGroup.size(); j++) {
                for (final Map.Entry<String, BinaryClassRelationship> entry : allRelationships.entrySet()) {
                    if (compositionMatches >= 2) {
                        break;
                    }
                    final BinaryClassRelationship bCR = entry.getValue();
                    if (bCR.getClassA().uniqueName().equals(componentRelatedGroup.get(j).uniqueName())
                            && (bCR.getaSideAssociation() == BinaryClassAssociation.COMPOSITION
                            || bCR.getbSideAssociation() == BinaryClassAssociation.COMPOSITION)) {
                        if (!componentRelatedGroup.contains(bCR.getClassB())) {
                            componentRelatedGroup.add(bCR.getClassB());
                            compositionMatches++;
                        }
                    }
                    if (bCR.getClassB().uniqueName().equals(componentRelatedGroup.get(j).uniqueName())
                            && (bCR.getaSideAssociation() == BinaryClassAssociation.COMPOSITION
                            || bCR.getbSideAssociation() == BinaryClassAssociation.COMPOSITION)) {
                        if (!componentRelatedGroup.contains(bCR.getClassA())) {
                            componentRelatedGroup.add(bCR.getClassA());
                            compositionMatches++;
                        }
                    }
                }
            }
            /**
             * Filter stage 5: get any remaining components that are involved in
             * a extension/realization relationship with the list of components
             * collected so far.
             */
            int remainingAbstractMatches = 0;
            for (int j = 0; j < componentRelatedGroup.size(); j++) {
                for (final Map.Entry<String, BinaryClassRelationship> entry : allRelationships.entrySet()) {
                    if (remainingAbstractMatches >= 2) {
                        break;
                    }
                    final BinaryClassRelationship bCR = entry.getValue();
                    if (bCR.getClassA().uniqueName().equals(componentRelatedGroup.get(j).uniqueName())
                            && (bCR.getaSideAssociation() == BinaryClassAssociation.GENERALISATION
                            || bCR.getaSideAssociation() == BinaryClassAssociation.REALIZATION
                            || bCR.getbSideAssociation() == BinaryClassAssociation.GENERALISATION
                            || bCR.getbSideAssociation() == BinaryClassAssociation.REALIZATION)) {
                        if (!componentRelatedGroup.contains(bCR.getClassB())) {
                            componentRelatedGroup.add(bCR.getClassB());
                            remainingAbstractMatches++;

                        }
                    }
                    if (bCR.getClassB().uniqueName().equals(componentRelatedGroup.get(j).uniqueName())
                            && (bCR.getbSideAssociation() == BinaryClassAssociation.GENERALISATION
                            || bCR.getbSideAssociation() == BinaryClassAssociation.REALIZATION
                            || bCR.getaSideAssociation() == BinaryClassAssociation.GENERALISATION
                            || bCR.getaSideAssociation() == BinaryClassAssociation.REALIZATION)) {
                        if (!componentRelatedGroup.contains(bCR.getClassA())) {
                            componentRelatedGroup.add(bCR.getClassA());
                            remainingAbstractMatches++;
                        }
                    }
                }
            }

            // add all the components related to the current component to the
            // overall component group
            overallRelatedGroup.addAll(componentRelatedGroup);
        }

        /**
         * Filter stage 6: If there is space, add any remaining weak
         * relationships.
         */
        final List<Component> componentRelatedGroup = new ArrayList<Component>(overallRelatedGroup);
        double desiredSize = (5.0 * (Math.pow(mainComponents.size(), 0.5)));
        for (Component tmpCmp : componentRelatedGroup) {
            for (final Map.Entry<String, BinaryClassRelationship> entry : allRelationships.entrySet()) {

                if (overallRelatedGroup.size() >= desiredSize) {
                    break;
                }

                final BinaryClassRelationship bCR = entry.getValue();
                if (bCR.getClassA().componentType() == OOPSourceModelConstants.ComponentType.INTERFACE
                        || bCR.getClassA().modifiers().contains("abstract")
                        || bCR.getClassB().componentType() == OOPSourceModelConstants.ComponentType.INTERFACE
                        || bCR.getClassB().modifiers().contains("abstract")) {

                    if (bCR.getClassA().uniqueName().equals(tmpCmp.uniqueName())
                            && (bCR.getaSideAssociation() == BinaryClassAssociation.WEAK_ASSOCIATION
                            || bCR.getbSideAssociation() == BinaryClassAssociation.AGGREGATION
                            || bCR.getbSideAssociation() == BinaryClassAssociation.WEAK_ASSOCIATION
                            || bCR.getaSideAssociation() == BinaryClassAssociation.AGGREGATION)) {
                        if (!componentRelatedGroup.contains(bCR.getClassB())) {
                            overallRelatedGroup.add(bCR.getClassB());
                        }
                    }
                    if (bCR.getClassB().uniqueName().equals(tmpCmp.uniqueName())
                            && (bCR.getaSideAssociation() == BinaryClassAssociation.WEAK_ASSOCIATION
                            || bCR.getbSideAssociation() == BinaryClassAssociation.AGGREGATION
                            || bCR.getbSideAssociation() == BinaryClassAssociation.WEAK_ASSOCIATION
                            || bCR.getaSideAssociation() == BinaryClassAssociation.AGGREGATION)) {
                        if (!componentRelatedGroup.contains(bCR.getClassA())) {
                            overallRelatedGroup.add(bCR.getClassA());
                        }
                    }
                }
            }
        }

        for (Component tmpCmp : componentRelatedGroup) {
            for (final Map.Entry<String, BinaryClassRelationship> entry : allRelationships.entrySet()) {

                if (overallRelatedGroup.size() >= desiredSize) {
                    break;
                }

                final BinaryClassRelationship bCR = entry.getValue();

                if (bCR.getClassA().uniqueName().equals(tmpCmp.uniqueName())
                        && (bCR.getaSideAssociation() == BinaryClassAssociation.WEAK_ASSOCIATION
                        || bCR.getbSideAssociation() == BinaryClassAssociation.AGGREGATION
                        || bCR.getbSideAssociation() == BinaryClassAssociation.WEAK_ASSOCIATION
                        || bCR.getaSideAssociation() == BinaryClassAssociation.AGGREGATION)) {
                    if (!componentRelatedGroup.contains(bCR.getClassB())) {
                        overallRelatedGroup.add(bCR.getClassB());
                    }
                }
                if (bCR.getClassB().uniqueName().equals(tmpCmp.uniqueName())
                        && (bCR.getaSideAssociation() == BinaryClassAssociation.WEAK_ASSOCIATION
                        || bCR.getbSideAssociation() == BinaryClassAssociation.AGGREGATION
                        || bCR.getbSideAssociation() == BinaryClassAssociation.WEAK_ASSOCIATION
                        || bCR.getaSideAssociation() == BinaryClassAssociation.AGGREGATION)) {
                    if (!componentRelatedGroup.contains(bCR.getClassA())) {
                        overallRelatedGroup.add(bCR.getClassA());
                    }
                }
            }
        }
        // remove duplicate components by using a set.
        return new HashSet<Component>(overallRelatedGroup);
    }
}
