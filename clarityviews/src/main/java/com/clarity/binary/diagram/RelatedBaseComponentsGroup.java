package com.clarity.binary.diagram;

import com.clarity.binary.diagram.DiagramConstants.BinaryClassAssociation;
import com.clarity.binary.extractor.BinaryClassRelationship;
import com.clarity.invocation.ComponentInvocation;
import com.clarity.invocation.TypeExtension;
import com.clarity.invocation.TypeImplementation;
import com.clarity.sourcemodel.Component;
import com.clarity.sourcemodel.OOPSourceModelConstants;
import net.sf.oval.constraint.NotEmpty;
import net.sf.oval.constraint.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Represents a group of base components that are related to each other.
 */
public class RelatedBaseComponentsGroup {

    private double desiredSize;

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
        this.desiredSize = (5.0 * (Math.pow(mainComponents.size(), 0.5)));
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
        this.mainComponents = sortComponentsByPopularity(addedBaseComponents, allComponents);
        this.desiredSize = (5.0 * (Math.pow(mainComponents.size(), 0.5)));
    }

    private static Map<String, Integer> sortByComparator(Map<String, Integer> unsortMap, final boolean order) {

        List<Map.Entry<String, Integer>> list = new LinkedList<Map.Entry<String, Integer>>(unsortMap.entrySet());

        // Sorting the list based on values
        Collections.sort(list, new Comparator<Map.Entry<String, Integer>>() {
            public int compare(Map.Entry<String, Integer> o1,
                               Map.Entry<String, Integer> o2) {
                if (order) {
                    return o1.getValue().compareTo(o2.getValue());
                } else {
                    return o2.getValue().compareTo(o1.getValue());

                }
            }
        });

        // Maintaining insertion order with the help of LinkedList
        Map<String, Integer> sortedMap = new LinkedHashMap<String, Integer>();
        for (Map.Entry<String, Integer> entry : list) {
            sortedMap.put(entry.getKey(), entry.getValue());
        }

        return sortedMap;
    }

    /**
     * Creates a list of components who are closely related to the given
     * components.
     */
    public Set<Component> components() {

        Set<String> relatedComponentsSetNames = new HashSet(mainComponents);

        for (int i = 0; i < 3 && relatedComponentsSetNames.size() <= desiredSize; i++) {

            relatedComponentsSetNames = sortComponentsByPopularity(relatedComponentsSetNames, allComponents);
            Set<String> relatedComponentsSetNamesCopy = new HashSet<>(relatedComponentsSetNames);
            // loop in order of insertion order...
            Iterator<String> itr = relatedComponentsSetNamesCopy.iterator();
            while (itr.hasNext() && relatedComponentsSetNames.size() <= desiredSize) {

                String cmpName = itr.next();
                Component cmp = allComponents.get(cmpName);
                final String shortName = cmp.name();
                // create a filtered map for all relationships relevant to the
                // current loop Component.
                Map<String, BinaryClassRelationship> relevantBinaryRelationships = allRelationships.entrySet().stream()
                        .filter(map -> map.getKey().contains(shortName))
                        .collect(Collectors.toMap(p -> p.getKey(), p -> p.getValue()));

                /**
                 * Filter stage 1: form a super class hierarchy chain of the key
                 * component.
                 */
                final List<Component> tmpSuperComponentRelatedGroup = new ArrayList<Component>();
                tmpSuperComponentRelatedGroup.add(cmp);
                for (int j = 0; j < tmpSuperComponentRelatedGroup.size() && relatedComponentsSetNames.size() <= desiredSize; j++) {
                    for (final Map.Entry<String, BinaryClassRelationship> entry : relevantBinaryRelationships.entrySet()) {

                        if (relatedComponentsSetNames.size() > desiredSize) {
                            break;
                        }

                        final BinaryClassRelationship bCR = entry.getValue();

                        if (bCR.getClassA().uniqueName().equals(tmpSuperComponentRelatedGroup.get(j).uniqueName())
                                && (bCR.getaSideAssociation() == BinaryClassAssociation.GENERALISATION
                                || bCR.getaSideAssociation() == BinaryClassAssociation.REALIZATION)) {
                            if (!tmpSuperComponentRelatedGroup.contains(bCR.getClassB())) {
                                relatedComponentsSetNames.add(bCR.getClassB().uniqueName());
                                tmpSuperComponentRelatedGroup.add(bCR.getClassB());
                            }
                        }

                        if (bCR.getClassB().uniqueName().equals(tmpSuperComponentRelatedGroup.get(j).uniqueName())
                                && (bCR.getbSideAssociation() == BinaryClassAssociation.GENERALISATION
                                || bCR.getbSideAssociation() == BinaryClassAssociation.REALIZATION)) {
                            if (!tmpSuperComponentRelatedGroup.contains(bCR.getClassA())) {
                                relatedComponentsSetNames.add(bCR.getClassA().uniqueName());
                                tmpSuperComponentRelatedGroup.add(bCR.getClassA());
                            }
                        }

                    }
                }

                /**
                 * Filter stage 2: form a sub class hierarchy chain of the key
                 * component.
                 */
                final List<Component> tmpSubComponentRelatedGroup = new ArrayList<>();
                tmpSubComponentRelatedGroup.add(cmp);
                for (int j = 0; j < tmpSubComponentRelatedGroup.size() && relatedComponentsSetNames.size() <= desiredSize; j++) {
                    for (final Map.Entry<String, BinaryClassRelationship> entry : relevantBinaryRelationships.entrySet()) {
                        if (relatedComponentsSetNames.size() > desiredSize) {
                            break;
                        }

                        final BinaryClassRelationship bCR = entry.getValue();

                        if (bCR.getClassA().uniqueName().equals(tmpSubComponentRelatedGroup.get(j).uniqueName())
                                && (bCR.getbSideAssociation() == BinaryClassAssociation.GENERALISATION
                                || bCR.getbSideAssociation() == BinaryClassAssociation.REALIZATION)) {
                            if (!tmpSubComponentRelatedGroup.contains(bCR.getClassB())) {
                                relatedComponentsSetNames.add(bCR.getClassB().uniqueName());
                                tmpSubComponentRelatedGroup.add(bCR.getClassB());
                            }
                        }

                        if (bCR.getClassB().uniqueName().equals(tmpSubComponentRelatedGroup.get(j).uniqueName())
                                && (bCR.getaSideAssociation() == BinaryClassAssociation.GENERALISATION
                                || bCR.getaSideAssociation() == BinaryClassAssociation.REALIZATION)) {
                            if (!tmpSubComponentRelatedGroup.contains(bCR.getClassA())) {
                                tmpSubComponentRelatedGroup.add(bCR.getClassA());
                                relatedComponentsSetNames.add(bCR.getClassA().uniqueName());
                            }
                        }
                    }
                }

                /**
                 * Filter stage 3: Any components mentioned by documentation should be included.
                 */

                if (relatedComponentsSetNames.size() <= desiredSize) {

                    for (ComponentInvocation docMention : cmp.componentInvocations(OOPSourceModelConstants.ComponentInvocations.DOC_MENTION)) {
                        Component mentionedComponent = allComponents.get(docMention.invokedComponent());
                        if (mentionedComponent != null) {
                            relatedComponentsSetNames.add(mentionedComponent.uniqueName());
                        }
                    }
                }
                /**
                 * Filter stage 4: Start with the beginning of the current result
                 * list and look for composition relationships.
                 */

                for (final Map.Entry<String, BinaryClassRelationship> entry : allRelationships.entrySet()) {
                    if (relatedComponentsSetNames.size() > desiredSize) {
                        break;
                    }
                    final BinaryClassRelationship bCR = entry.getValue();
                    if (bCR.getClassA().uniqueName().equals(cmp.uniqueName())
                            && (bCR.getaSideAssociation() == BinaryClassAssociation.COMPOSITION
                            || bCR.getbSideAssociation() == BinaryClassAssociation.COMPOSITION)) {
                        relatedComponentsSetNames.add(bCR.getClassB().uniqueName());

                    }
                    if (bCR.getClassB().uniqueName().equals(cmp.uniqueName())
                            && (bCR.getaSideAssociation() == BinaryClassAssociation.COMPOSITION
                            || bCR.getbSideAssociation() == BinaryClassAssociation.COMPOSITION)) {
                        relatedComponentsSetNames.add(bCR.getClassA().uniqueName());
                    }
                }
            }

            /**
             * Filter stage 6: If there is space, add any remaining weak
             * relationships.
             */
            final List<Component> componentRelatedGroup = new ArrayList<Component>();
            relatedComponentsSetNames.forEach((k) -> componentRelatedGroup.add(allComponents.get(k)));
            for (Component tmpCmp : componentRelatedGroup) {
                for (final Map.Entry<String, BinaryClassRelationship> entry : allRelationships.entrySet()) {

                    if (relatedComponentsSetNames.size() > desiredSize) {
                        break;
                    }

                    final BinaryClassRelationship bCR = entry.getValue();

                    if (bCR.getClassA().uniqueName().equals(tmpCmp.uniqueName())
                            && (bCR.getaSideAssociation() == BinaryClassAssociation.WEAK_ASSOCIATION
                            || bCR.getbSideAssociation() == BinaryClassAssociation.AGGREGATION
                            || bCR.getbSideAssociation() == BinaryClassAssociation.WEAK_ASSOCIATION
                            || bCR.getaSideAssociation() == BinaryClassAssociation.AGGREGATION)) {
                        relatedComponentsSetNames.add(bCR.getClassB().uniqueName());

                    }
                    if (bCR.getClassB().uniqueName().equals(tmpCmp.uniqueName())
                            && (bCR.getaSideAssociation() == BinaryClassAssociation.WEAK_ASSOCIATION
                            || bCR.getbSideAssociation() == BinaryClassAssociation.AGGREGATION
                            || bCR.getbSideAssociation() == BinaryClassAssociation.WEAK_ASSOCIATION
                            || bCR.getaSideAssociation() == BinaryClassAssociation.AGGREGATION)) {
                        relatedComponentsSetNames.add(bCR.getClassA().uniqueName());

                    }
                }
            }
        }

        // Return a list of components representing the collected component names
        Set<Component> relatedComponents = new HashSet<>();
        relatedComponentsSetNames.forEach((v) -> relatedComponents.add(allComponents.get(v)));
        return relatedComponents;
    }

    /**
     * Returns a list of components sorted by their popularity amongst each other in descending order.
     */
    private Set<String> sortComponentsByPopularity(Set<String> componentNamesList, Map<String, Component> codebase) {

        final Map<String, Integer> componentScorePairs = new HashMap<>();
        componentNamesList.forEach((currComponentName) -> {
            Component currCmp = codebase.get(currComponentName);
            int score = 0;
            for (String tmpCmpName : componentNamesList) {
                Component tmpCmp = codebase.get(tmpCmpName);
                if (!currComponentName.equals(tmpCmpName)) {
                    for (ComponentInvocation invocation : tmpCmp.invocations()) {
                        if (invocation.invokedComponent().equals(currCmp.uniqueName())) {
                            if (invocation instanceof TypeExtension || invocation instanceof TypeImplementation) {
                                score += 3;
                            } else {
                                score += 1;
                            }
                        }
                    }
                }
            }
            componentScorePairs.put(currCmp.uniqueName(), score);
        });

        Map<String, Integer> sortedComponentScoresMap = sortByComparator(componentScorePairs, false);
        // now that we have a map containing component-score pairs, sort and return.
        Set<String> sortedComponents = new LinkedHashSet<>();
        for (Map.Entry<String, Integer> entry : sortedComponentScoresMap.entrySet()) {
            sortedComponents.add(entry.getKey());
        }
        return sortedComponents;
    }

}
