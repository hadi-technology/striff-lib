package com.hadi.striff.extractor;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.hadi.clarpse.sourcemodel.Component;
import com.hadi.striff.diagram.DiagramComponent;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * Immutable map of all relationships in a codebase.
 *
 * <p>This class provides efficient storage and retrieval of component relationships
 * extracted from an {@link com.hadi.clarpse.sourcemodel.OOPSourceCodeModel}.
 * Relationships are organized by source component, then by target component, with
 * a {@link TreeSet} for each target to enable efficient retrieval of the most
 * significant relationship.</p>
 *
 * <p><strong>Key optimization:</strong> This map enables extracting relationships
 * once on a merged model, then filtering by component names to obtain old/new
 * relations. This avoids redundant extraction that would occur if extracting
 * separately from old and new models.</p>
 *
 * The internal structure is:
 * <pre>Map&lt;sourceComponentName, Map&lt;targetComponent, TreeSet&lt;Relation&gt;&gt;&gt;</pre>
 */
public class RelationsMap {

    /**
     * Map< originalComponentUniqueName, Map< targetComponent, TreeSet<Relation> > >.
     */
    private Map<String, Map<Component, TreeSet<ComponentRelation>>> relMap = new HashMap<>();
    private int size = 0;

    /**
     * Creates an empty RelationsMap.
     */
    public RelationsMap() {
        this.relMap = new HashMap<>();
    }

    /**
     * Creates a RelationsMap from an existing relation map.
     *
     * @param relMap the pre-populated relation map
     */
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

    /**
     * Inserts a relation into the map.
     *
     * <p>Relations are stored in a {@link TreeSet} per (source, target) pair,
     * enabling efficient retrieval of the most significant relation via
     * {@link #mostSignificantRelation(Component, Component)}.</p>
     *
     * @param rel the relation to insert
     */
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

    /**
     * Returns the most significant relationship between two components.
     *
     * <p>When multiple relations exist between the same source and target,
     * this method returns the one with the highest strength. The strength
     * is determined by the {@link ComponentRelation#strength()} method.</p>
     *
     * @param original the source component
     * @param target   the target component
     * @return the most significant relation, or an empty relation if not related
     */
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

    /**
     * Returns a new RelationsMap containing only relationships where both the source
     * and target components are in the provided filter set.
     *
     * <p>This method is a key optimization for {@link com.hadi.striff.parse.CodeDiff},
     * enabling extraction of relationships once on the merged model, then filtering
     * to obtain old/new relations without re-extracting from the original models.</p>
     *
     * <p>The filter applies to both the source and target components of each relation.
     * A relation is included only if both its source and target component names are
     * present in the filter set.</p>
     *
     * @param filterCmps set of component names to include in the filtered result
     * @return a new RelationsMap with filtered relationships
     */
    public RelationsMap filteredRelations(Set<String> filterCmps) {
        Map<String, Map<Component, TreeSet<ComponentRelation>>> filteredRelMap = new HashMap<>(this.relMap);
        filteredRelMap.keySet().removeIf(key -> !filterCmps.contains(key));
        for (Map.Entry<String, Map<Component, TreeSet<ComponentRelation>>> entry : filteredRelMap.entrySet()) {
            entry.getValue().keySet().removeIf(key -> !filterCmps.contains(key.uniqueName()));
        }
        return new RelationsMap(filteredRelMap);
    }
}
