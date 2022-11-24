package com.hadii.striff.diagram;

import com.google.common.collect.Sets;
import com.hadii.striff.ChangeSet;
import com.hadii.striff.extractor.ComponentRelation;
import com.hadii.striff.parse.CodeDiff;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Represents the set of components and relations that are to be displayed in a Striff diagram.
 */
public class StriffDiagramModel {

    private final Set<DiagramComponent> diagramCmps = new HashSet<>();
    private Set<ComponentRelation> coreRelations = new HashSet<>();
    private final Set<ComponentRelation> contextRelations = new HashSet<>();

    public StriffDiagramModel(CodeDiff codeDiff) {
        this(codeDiff, Collections.emptySet());
    }

    public StriffDiagramModel(CodeDiff codeDiff, Set<String> sourceFilesFilter) {
        calculateCoreBaseCmps(codeDiff, sourceFilesFilter);
        calculateCoreRels(codeDiff.changeSet());
    }

    private void calculateCoreBaseCmps(CodeDiff codeDiff, Set<String> sourceFilesFilter) {
        ChangeSet changeSet = codeDiff.changeSet();
        Set<DiagramComponent> unfilteredCoreCmps = Stream.of(
            changeSet.addedComponents(),
            changeSet.deletedComponents(),
            changeSet.keyRelationsComponents(),
            changeSet.modifiedComponents()).flatMap(Collection::stream).collect(Collectors.toSet());
        if (!sourceFilesFilter.isEmpty()) {
            unfilteredCoreCmps = unfilteredCoreCmps.stream().filter(cmp -> sourceFilesFilter.contains(
                cmp.sourceFile())).collect(Collectors.toSet());
        }
        unfilteredCoreCmps.forEach(diagramComponent -> {
            if (diagramComponent.componentType().isBaseComponent()) {
                this.diagramCmps.add(diagramComponent);
            } else {
                DiagramComponent parentComponent = diagramComponent.parentBaseCmp(
                    codeDiff.components());
                if (parentComponent != null) {
                    this.diagramCmps.add(parentComponent);
                }
            }
        });
    }

    private void calculateCoreRels(ChangeSet changeSet) {
        this.coreRelations = Stream.of(changeSet.addedRelations().allRels(),
                                       changeSet.deletedRelations().allRels())
                                   .flatMap(Collection::stream)
                                   .collect(Collectors.toSet());
    }

    public Set<DiagramComponent> allBaseCmps() {
        return this.diagramCmps.stream().filter(
            cmp -> cmp.componentType().isBaseComponent()).collect(Collectors.toSet());
    }

    public Set<DiagramComponent> diagramCmps() {
        return this.diagramCmps;
    }

    public Set<ComponentRelation> coreRels() {
        return this.coreRelations;
    }

    public Set<ComponentRelation> contextRels() {
        return this.contextRelations;
    }

    public Set<ComponentRelation> allRels() {
        return Sets.union(this.contextRelations, this.coreRelations);
    }
}
