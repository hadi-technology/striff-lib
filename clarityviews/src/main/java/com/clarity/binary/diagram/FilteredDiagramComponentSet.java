package com.clarity.binary.diagram;

import com.clarity.binary.diagram.DiagramConstants.BinaryClassAssociation;
import com.clarity.binary.extractor.BinaryClassRelationship;
import com.clarity.invocation.ComponentInvocation;
import com.clarity.invocation.TypeDeclaration;
import com.clarity.invocation.TypeExtension;
import com.clarity.invocation.TypeImplementation;
import com.clarity.sourcemodel.OOPSourceModelConstants;

import java.util.ArrayList;
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
 * Represents the set of components to be displayed on the final structure-diff diagram.
 */
public class FilteredDiagramComponentSet {

    private final List<String> addedComponents;
    private final List<String> deletedComponents;
    private final List<String> modifiedComponents;
    private final List<String> modifiedRelationshipComponents;
    private double diagramSize;
    private final Set<String> mainComponents = new HashSet<>();
    private Map<String, DiagramComponent> allComponents;
    private List<BinaryClassRelationship> allRelationships;

    public FilteredDiagramComponentSet(final Map<String, DiagramComponent> allComponents,
                                       final List<BinaryClassRelationship> allRelationships,
                                       final List<String> addedComponents,
                                       final List<String> deletedComponents,
                                       final List<String> modifiedComponents,
                                       final List<String> modifiedRelationshipComponents) {
        this.allComponents = allComponents;
        this.allRelationships = allRelationships;
        this.addedComponents = addedComponents;
        this.deletedComponents = deletedComponents;
        this.modifiedComponents = modifiedComponents;
        this.modifiedRelationshipComponents = modifiedRelationshipComponents;

        // added components set filtered for base components only.
        Set<String> addedBaseComponents = new HashSet<>();
        addedComponents.forEach(s -> {
            DiagramComponent cmp = allComponents.get(s);
            if (cmp.componentType() != OOPSourceModelConstants.ComponentType.LOCAL) {
                while (cmp != null && !cmp.componentType().isBaseComponent()) {
                    cmp = allComponents.get(cmp.parentUniqueName());
                }
                if (cmp != null && cmp.componentType().isBaseComponent()) {
                    mainComponents.add(cmp.uniqueName());
                }
            }
        });
        this.mainComponents.addAll(addedBaseComponents);

        // deleted components set filtered for base components only.
        deletedComponents.forEach(s -> {
            DiagramComponent cmp = allComponents.get(s);
            if (cmp.componentType() != OOPSourceModelConstants.ComponentType.LOCAL) {
                while (cmp != null && !cmp.componentType().isBaseComponent()) {
                    cmp = allComponents.get(cmp.parentUniqueName());
                }
                if (cmp != null && cmp.componentType().isBaseComponent()) {
                    mainComponents.add(cmp.uniqueName());
                }
            }
        });

        // modified components set filtered for base components only.
        modifiedComponents.forEach(s -> {
            DiagramComponent cmp = allComponents.get(s);
            if (cmp.componentType() != OOPSourceModelConstants.ComponentType.LOCAL) {
                while (cmp != null && !cmp.componentType().isBaseComponent()) {
                    cmp = allComponents.get(cmp.parentUniqueName());
                }
                if (cmp != null && cmp.componentType().isBaseComponent()) {
                    mainComponents.add(cmp.uniqueName());
                }
            }
        });

        this.mainComponents.addAll(modifiedRelationshipComponents);
        int diagramSize = (int) (5 * Math.pow(mainComponents.size(), 0.5));
        if (diagramSize > 15) {
            diagramSize = 15;
        }
        this.diagramSize = diagramSize;
    }

    private static Map<String, Integer> sortByComparator(Map<String, Integer> unsortMap, final boolean order) {
        List<Map.Entry<String, Integer>> list = new LinkedList<>(unsortMap.entrySet());
        // Sorting the list based on values
        list.sort((o1, o2) -> {
            if (order) {
                return o1.getValue().compareTo(o2.getValue());
            } else {
                return o2.getValue().compareTo(o1.getValue());

            }
        });

        // Maintaining insertion order with the help of LinkedList
        Map<String, Integer> sortedMap = new LinkedHashMap<>();
        for (Map.Entry<String, Integer> entry : list) {
            sortedMap.put(entry.getKey(), entry.getValue());
        }
        return sortedMap;
    }

    /**
     * Creates a list of components to be displayed on the final structure-diff diagram.
     */
    public Set<DiagramComponent> components() {

        Set<String> relatedComponentsSetNames = new HashSet(mainComponents);
        relatedComponentsSetNames = sortComponentsByPopularity(relatedComponentsSetNames, allComponents);
        if (relatedComponentsSetNames.size() > diagramSize) {
            relatedComponentsSetNames = relatedComponentsSetNames.stream().limit((int) (diagramSize)).collect(Collectors.toSet());
        } else {
            for (int i = 0; i < 3 && relatedComponentsSetNames.size() <= diagramSize; i++) {
                relatedComponentsSetNames = sortComponentsByPopularity(relatedComponentsSetNames, allComponents);
                Set<String> relatedComponentsSetNamesCopy = new HashSet<>(relatedComponentsSetNames);
                // loop in order of insertion order...
                Iterator<String> itr = relatedComponentsSetNamesCopy.iterator();
                while (itr.hasNext() && relatedComponentsSetNames.size() <= diagramSize) {
                    String cmpName = itr.next();
                    DiagramComponent cmp = allComponents.get(cmpName);
                    // create a filtered map for all relationships relevant to the
                    // current loop Component.
                    List<BinaryClassRelationship> relevantBinaryRelationships = allRelationships.stream()
                            .filter(value -> value.getClassA().uniqueName().equals(cmp.uniqueName())
                                    || value.getClassB().uniqueName().equals(cmp.uniqueName()))
                            .collect(Collectors.toCollection(ArrayList::new));

                    /**
                     * Filter stage 1: form a super class hierarchy chain of the key
                     * component.
                     */
                    final List<DiagramComponent> tmpSuperComponentRelatedGroup = new ArrayList<>();
                    tmpSuperComponentRelatedGroup.add(cmp);
                    for (int j = 0; j < tmpSuperComponentRelatedGroup.size() && relatedComponentsSetNames.size() <= diagramSize; j++) {
                        for (BinaryClassRelationship entry : relevantBinaryRelationships) {
                            if (relatedComponentsSetNames.size() > diagramSize) {
                                break;
                            }
                            if (entry.getClassA().uniqueName().equals(tmpSuperComponentRelatedGroup.get(j).uniqueName())
                                    && (entry.getaSideAssociation() == BinaryClassAssociation.GENERALISATION
                                    || entry.getaSideAssociation() == BinaryClassAssociation.REALIZATION)) {
                                if (!tmpSuperComponentRelatedGroup.contains(entry.getClassB())) {
                                    relatedComponentsSetNames.add(entry.getClassB().uniqueName());
                                    tmpSuperComponentRelatedGroup.add(entry.getClassB());
                                }
                            }

                            if (entry.getClassB().uniqueName().equals(tmpSuperComponentRelatedGroup.get(j).uniqueName())
                                    && (entry.getbSideAssociation() == BinaryClassAssociation.GENERALISATION
                                    || entry.getbSideAssociation() == BinaryClassAssociation.REALIZATION)) {
                                if (!tmpSuperComponentRelatedGroup.contains(entry.getClassA())) {
                                    relatedComponentsSetNames.add(entry.getClassA().uniqueName());
                                    tmpSuperComponentRelatedGroup.add(entry.getClassA());
                                }
                            }

                        }
                    }

                    /**
                     * Filter stage 2: form a sub class hierarchy chain of the key
                     * component.
                     */
                    final List<DiagramComponent> tmpSubComponentRelatedGroup = new ArrayList<>();
                    tmpSubComponentRelatedGroup.add(cmp);
                    for (int j = 0; j < tmpSubComponentRelatedGroup.size() && relatedComponentsSetNames.size() <= diagramSize; j++) {
                        for (BinaryClassRelationship entry : relevantBinaryRelationships) {
                            if (relatedComponentsSetNames.size() > diagramSize) {
                                break;
                            }
                            if (entry.getClassA().uniqueName().equals(tmpSubComponentRelatedGroup.get(j).uniqueName())
                                    && (entry.getbSideAssociation() == BinaryClassAssociation.GENERALISATION
                                    || entry.getbSideAssociation() == BinaryClassAssociation.REALIZATION)) {
                                if (!tmpSubComponentRelatedGroup.contains(entry.getClassB())) {
                                    relatedComponentsSetNames.add(entry.getClassB().uniqueName());
                                    tmpSubComponentRelatedGroup.add(entry.getClassB());
                                }
                            }

                            if (entry.getClassB().uniqueName().equals(tmpSubComponentRelatedGroup.get(j).uniqueName())
                                    && (entry.getaSideAssociation() == BinaryClassAssociation.GENERALISATION
                                    || entry.getaSideAssociation() == BinaryClassAssociation.REALIZATION)) {
                                if (!tmpSubComponentRelatedGroup.contains(entry.getClassA())) {
                                    tmpSubComponentRelatedGroup.add(entry.getClassA());
                                    relatedComponentsSetNames.add(entry.getClassA().uniqueName());
                                }
                            }
                        }
                    }

                    /**
                     * Filter stage 3: Any components mentioned by documentation should be included.
                     */

                    if (relatedComponentsSetNames.size() <= diagramSize) {

                        for (ComponentInvocation docMention : cmp.componentInvocations(OOPSourceModelConstants.ComponentInvocations.DOC_MENTION)) {
                            DiagramComponent mentionedComponent = allComponents.get(docMention.invokedComponent());
                            if (mentionedComponent != null) {
                                relatedComponentsSetNames.add(mentionedComponent.uniqueName());
                            }
                        }
                    }
                    /**
                     * Filter stage 4: Start with the beginning of the current result
                     * list and look for composition relationships.
                     */

                    for (BinaryClassRelationship entry : relevantBinaryRelationships) {
                        if (relatedComponentsSetNames.size() > diagramSize) {
                            break;
                        }
                        if (entry.getClassA().uniqueName().equals(cmp.uniqueName())
                                && (entry.getaSideAssociation() == BinaryClassAssociation.COMPOSITION
                                || entry.getbSideAssociation() == BinaryClassAssociation.COMPOSITION)) {
                            relatedComponentsSetNames.add(entry.getClassB().uniqueName());

                        }
                        if (entry.getClassB().uniqueName().equals(cmp.uniqueName())
                                && (entry.getaSideAssociation() == BinaryClassAssociation.COMPOSITION
                                || entry.getbSideAssociation() == BinaryClassAssociation.COMPOSITION)) {
                            relatedComponentsSetNames.add(entry.getClassA().uniqueName());
                        }
                    }
                }

                /**
                 * Filter stage 6: If there is space, add any remaining weak
                 * relationships.
                 */
                final List<DiagramComponent> componentRelatedGroup = new ArrayList<>();
                relatedComponentsSetNames.forEach((k) -> componentRelatedGroup.add(allComponents.get(k)));
                for (DiagramComponent tmpCmp : componentRelatedGroup) {
                    for (BinaryClassRelationship entry : allRelationships) {

                        if (relatedComponentsSetNames.size() > diagramSize) {
                            break;
                        }

                        if (entry.getClassA().uniqueName().equals(tmpCmp.uniqueName())
                                && (entry.getaSideAssociation() == BinaryClassAssociation.WEAK_ASSOCIATION
                                || entry.getbSideAssociation() == BinaryClassAssociation.AGGREGATION
                                || entry.getbSideAssociation() == BinaryClassAssociation.WEAK_ASSOCIATION
                                || entry.getaSideAssociation() == BinaryClassAssociation.AGGREGATION)) {
                            relatedComponentsSetNames.add(entry.getClassB().uniqueName());

                        }
                        if (entry.getClassB().uniqueName().equals(tmpCmp.uniqueName())
                                && (entry.getaSideAssociation() == BinaryClassAssociation.WEAK_ASSOCIATION
                                || entry.getbSideAssociation() == BinaryClassAssociation.AGGREGATION
                                || entry.getbSideAssociation() == BinaryClassAssociation.WEAK_ASSOCIATION
                                || entry.getaSideAssociation() == BinaryClassAssociation.AGGREGATION)) {
                            relatedComponentsSetNames.add(entry.getClassA().uniqueName());

                        }
                    }
                }
            }
        }

        // Return a list of components representing the collected component names
        Set<DiagramComponent> relatedComponents = new HashSet<>();
        relatedComponentsSetNames.forEach((v) -> relatedComponents.add(allComponents.get(v)));
        return relatedComponents;
    }

    /**
     * Returns a list of components sorted by how important they are to display.
     */
    private Set<String> sortComponentsByPopularity(Set<String> componentNamesList, Map<String, DiagramComponent> codebase) {

        final Map<String, Integer> componentScorePairs = new HashMap<>();
        componentNamesList.forEach((currComponentName) -> {
            DiagramComponent currCmp = codebase.get(currComponentName);
            int score = 0;
            for (String child : currCmp.children) {
                DiagramComponent childCmp = allComponents.get(child);
                if (addedComponents.contains(childCmp.uniqueName()) || deletedComponents.contains(childCmp.uniqueName())) {
                    score += 100;
                } else if (modifiedComponents.contains(childCmp.uniqueName())) {
                    score += 10;
                }
            }

            if (modifiedRelationshipComponents.contains(currCmp.uniqueName())) {
                score += 10;
            }

            for (String tmpCmpName : componentNamesList) {
                DiagramComponent tmpCmp = codebase.get(tmpCmpName);
                if (!currComponentName.equals(tmpCmpName)) {
                    for (ComponentInvocation invocation : tmpCmp.invocations()) {
                        if (invocation.invokedComponent().equals(currCmp.uniqueName())) {
                            if (invocation instanceof TypeExtension || invocation instanceof TypeImplementation) {
                                score += 4;
                            } else if (invocation instanceof TypeDeclaration && tmpCmp.componentType() == OOPSourceModelConstants.ComponentType.FIELD) {
                                score += 2;
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
