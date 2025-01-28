package com.hadii.striff.extractor;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.hadii.clarpse.sourcemodel.Component;
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
     * Map< originalComponentUniqueName, Map< targetComponent, TreeSet<Relation> > >.
     */
    private Map<String, Map<Component, TreeSet<ComponentRelation>>> relMap = new HashMap<>();
    private int size = 0;

    public RelationsMap() {
        this.relMap = new HashMap<>();
    }

    public RelationsMap(Map<String, Map<Component, TreeSet<ComponentRelation>>> relMap) {
        this.relMap = relMap;
    }

    @JsonProperty("relMap")
    public Map<String, Map<Component, TreeSet<ComponentRelation>>> relMap() {
        return this.relMap;
    }

    @JsonProperty("size")
    public int size() {
        return this.size;
    }

    @JsonIgnore
    public Set<ComponentRelation> rels(Component cmp) {
        if (hasRels(cmp.uniqueName())) {
            return this.relMap.get(cmp.uniqueName()).values().stream()
                    .flatMap(Set::stream)
                    .collect(Collectors.toSet());
        }
        return Collections.emptySet();
    }

    @JsonIgnore
    public Set<ComponentRelation> significantRels(Component cmp) {
        return significantRels(cmp.uniqueName());
    }

    @JsonIgnore
    public Set<ComponentRelation> significantRels(String cmpUniqueName) {
        if (hasRels(cmpUniqueName)) {
            HashSet<ComponentRelation> significantRels = new HashSet<>();
            this.relMap.get(cmpUniqueName).values().forEach(relSet -> significantRels.add(relSet.first()));
            return significantRels;
        } else {
            return Collections.emptySet();
        }
    }

    @JsonIgnore
    public Set<ComponentRelation> allRels() {
        Set<ComponentRelation> allRelations = new HashSet<>();
        this.relMap.values().forEach(
                targetMap -> allRelations.addAll(
                        targetMap.values().stream().flatMap(Set::stream).collect(Collectors.toSet())));
        return allRelations;
    }

    public void insertRelation(ComponentRelation rel) {
        String key = rel.originalComponent().uniqueName();
        if (!this.relMap.containsKey(key)) {
            this.relMap.put(key, new HashMap<>());
        }
        if (!this.relMap.get(key).containsKey(rel.targetComponent())) {
            this.relMap.get(key).put(rel.targetComponent(), new TreeSet<>());
        }
        this.relMap.get(key).get(rel.targetComponent()).add(rel);
        this.size++;
    }

    @JsonIgnore
    public boolean hasRels(String cmpUniqueName) {
        return this.relMap.containsKey(cmpUniqueName) && !this.relMap.get(cmpUniqueName).isEmpty();
    }

    @JsonIgnore
    public boolean contains(ComponentRelation rel) {
        String key = rel.originalComponent().uniqueName();
        return this.relMap.containsKey(key)
                && this.relMap.get(key).containsKey(rel.targetComponent())
                && this.relMap.get(key).get(rel.targetComponent()).contains(rel);
    }

    @JsonIgnore
    public Set<ComponentRelation> relsByType(DiagramConstants.ComponentAssociation type) {
        return this.allRels().stream()
                .filter(relation -> relation.associationType() == type)
                .collect(Collectors.toSet());
    }

    @JsonIgnore
    public boolean isRelated(Component origin, Component target) {
        return this.relMap.containsKey(origin.uniqueName())
                && this.relMap.get(origin.uniqueName()).containsKey(target)
                && !this.relMap.get(origin.uniqueName()).get(target).isEmpty();
    }

    @JsonIgnore
    public ComponentRelation mostSignificantRelation(Component original, Component target) {
        if (isRelated(original, target)) {
            return this.relMap.get(original.uniqueName()).get(target).stream()
                    .sorted(Comparator.comparing(ComponentRelation::strength).reversed())
                    .findFirst().orElse(new ComponentRelation());
        }
        return new ComponentRelation();
    }

    @JsonIgnore
    public Set<ComponentRelation> significantRels(Set<DiagramComponent> currPartition) {
        Set<ComponentRelation> relations = new HashSet<>();
        currPartition.forEach(cmp -> relations.addAll(this.significantRels(cmp.uniqueName())));
        return relations;
    }

    public RelationsMap filteredRelations(Set<String> filterCmps) {
        Map<String, Map<Component, TreeSet<ComponentRelation>>> filteredRelMap = new HashMap<>(this.relMap);
        filteredRelMap.keySet().removeIf(key -> !filterCmps.contains(key));
        for (Map.Entry<String, Map<Component, TreeSet<ComponentRelation>>> entry : filteredRelMap.entrySet()) {
            entry.getValue().keySet().removeIf(key -> !filterCmps.contains(key.uniqueName()));
        }
        return new RelationsMap(filteredRelMap);
    }
}
