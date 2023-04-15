package com.hadii.striff.extractor;

import com.hadii.striff.diagram.DiagramComponent;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

public class RelationsMap {

    /**
     * Map Structure.
     * Map < Original Component, Map < Target Component, List[Relation b/w Components] >>
     */
    private final Map<DiagramComponent, Map<DiagramComponent, TreeSet<ComponentRelation>>> relMap = new HashMap<>();

    private int size = 0;
    public Set<ComponentRelation> rels(DiagramComponent cmp) {
        if (hasRels(cmp)) {
            return this.relMap.get(cmp).values().stream().flatMap(Set::stream).collect(Collectors.toSet());
        }
        return Collections.emptySet();
    }

    /**
     * Returns the most significant relationship for every pair of the given component
     * and all other components it shares one or more relationships with.
     */
    public Set<ComponentRelation> significantRels(DiagramComponent cmp) {
        if (hasRels(cmp)) {
            // Given the relations are stored in a Treeset, the most significant relation
            // is at the top of the list.
            HashSet<ComponentRelation> significantRels = new HashSet<>();
            this.relMap.get(cmp).values().forEach(componentRelations -> significantRels.add(componentRelations.first()));
            return significantRels;
        } else {
            return Collections.emptySet();
        }
    }


    public Set<ComponentRelation> allRels() {
        Set<ComponentRelation> allRelations = new HashSet<>();
        this.relMap.values().forEach(diagramComponentSetMap -> allRelations.addAll(diagramComponentSetMap.values().stream().flatMap(Set::stream).collect(Collectors.toSet())));
        return allRelations;
    }

    public void insertRelation(ComponentRelation rel) {
        if (!this.relMap.containsKey(rel.originalComponent())) {
            this.relMap.put(rel.originalComponent(), new HashMap<>());
        }
        if (!this.relMap.get(rel.originalComponent()).containsKey(rel.targetComponent())) {
            this.relMap.get(rel.originalComponent()).put(rel.targetComponent(), new TreeSet<>());
        }
        this.relMap.get(rel.originalComponent()).get(rel.targetComponent()).add(rel);
        this.size += 1;
    }

    public int size() {
        return this.size;
    }
    public boolean hasRels(DiagramComponent cmp) {
        return this.relMap.containsKey(cmp) && !this.relMap.get(cmp).isEmpty();
    }

    public final boolean contains(ComponentRelation rel) {
        return this.relMap.containsKey(rel.originalComponent()) && this.relMap.get(rel.originalComponent()).containsKey(rel.targetComponent()) && this.relMap.get(rel.originalComponent()).get(rel.targetComponent()).contains(rel);
    }

    public final boolean hasRels(String cmpUniqueName) {
        return this.relMap.keySet().stream().filter(cmp -> cmp.uniqueName().equals(cmpUniqueName)).collect(Collectors.toSet()).size() == 1;
    }

    public final Set<ComponentRelation> relsByType(DiagramConstants.ComponentAssociation type) {
        return this.allRels().stream().filter(relation -> relation.associationType() == type).collect(Collectors.toSet());
    }

    public final boolean isRelated(DiagramComponent origin, DiagramComponent target) {
        return this.relMap.containsKey(origin) && this.relMap.get(origin).containsKey(target) && !this.relMap.get(origin).get(target).isEmpty();
    }

    /**
     * Returns the most significant relationship  between the given original
     * and target component if it exists, otherwise and empty object is returned.
     */
    public ComponentRelation mostSignificantRelation(DiagramComponent original,
                                                     DiagramComponent target) {
        ComponentRelation significantRel = new ComponentRelation();
        if (isRelated(original, target)) {
            significantRel =
                this.relMap.get(original).get(target).stream().sorted(Comparator.comparing(ComponentRelation::strength).reversed()).collect(Collectors.toList()).get(0);
        }
        return significantRel;
    }
}
