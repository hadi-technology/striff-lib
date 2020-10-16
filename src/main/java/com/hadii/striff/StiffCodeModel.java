package com.hadii.striff;

import com.google.common.collect.Sets;
import com.hadii.striff.diagram.DiagramComponent;
import com.hadii.striff.extractor.ComponentRelation;
import com.hadii.striff.parse.DiffCodeModel;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Represents the set of components and relations that are to be displayed in a Striff diagram.
 */
public class StiffCodeModel {

    private Set<DiagramComponent> contextComponents = new HashSet<>();
    private Set<DiagramComponent> coreComponents = new HashSet<>();
    private Set<ComponentRelation> coreRelations = new HashSet<>();
    private Set<ComponentRelation> contextRelations = new HashSet<>();

    public StiffCodeModel(DiffCodeModel mergedModel) {
        // Select the relevant base components from the change set
        populateCoreComponentsList(mergedModel);
        populateContextComponentsList(mergedModel);
        populateCoreRelationsList(mergedModel.changeSet());
        populateContextRelationsList(mergedModel);
    }

    public StiffCodeModel(DiffCodeModel mergedModel, List<String> sourceFilesFilter) {
        populateCoreComponentsList(mergedModel);
        this.coreComponents = this.coreComponents.stream().filter(diagramComponent -> sourceFilesFilter.contains(diagramComponent.sourceFile())).collect(Collectors.toSet());
        populateContextComponentsList(mergedModel);
        populateCoreRelationsList(mergedModel.changeSet());
        populateContextRelationsList(mergedModel);
    }

    private void populateCoreComponentsList(DiffCodeModel mergedModel) {
        ChangeSet changeSet = mergedModel.changeSet();
        Stream.of(changeSet.addedComponents(), changeSet.deletedComponents(), changeSet.keyRelationsComponents())
                .flatMap(Collection::stream).forEach(diagramComponent -> {
            if (diagramComponent.componentType().isBaseComponent()) {
                this.coreComponents.add(diagramComponent);
            } else {
                DiagramComponent parentComponent = diagramComponent.parentBaseComponent(mergedModel.mergedModel().components());
                if (parentComponent != null) {
                    this.coreComponents.add(parentComponent);
                }
            }
        });
    }

    private void populateCoreRelationsList(ChangeSet changeSet) {
        this.coreRelations.addAll(changeSet.addedRelations().relations());
        this.coreRelations.addAll(changeSet.deletedRelations().relations());
    }

    private void populateContextRelationsList(DiffCodeModel mergedModel) {
        this.allComponents().forEach(diagramComponent -> {
            if (mergedModel.relations().hasRelationsforComponent(diagramComponent)) {
                mergedModel.relations().componentRelations(diagramComponent).forEach(componentRelation -> {
                    if (this.allComponents().contains(componentRelation.targetComponent())
                            && !this.coreRelations.contains(componentRelation)) {
                        this.contextRelations.add(componentRelation);
                    }
                });
            }
        });
    }

    private void populateContextComponentsList(DiffCodeModel mergedModel) {
        // Add context components, defined as those components that have a relation to a core component.
        mergedModel.relations().relations().forEach(componentRelation -> {
            if (this.coreComponents.contains(componentRelation.targetComponent())
            && !mergedModel.changeSet().keyRelationsComponents().contains(componentRelation.originalComponent())) {
                this.contextComponents.add(componentRelation.originalComponent());
            } else if (this.coreComponents.contains(componentRelation.originalComponent())
            && !mergedModel.changeSet().keyRelationsComponents().contains(componentRelation.targetComponent())) {
                this.contextComponents.add(componentRelation.targetComponent());
            }
        });
    }

    public Set<DiagramComponent> allComponents() {
        return Sets.union(this.contextComponents, this.coreComponents);
    }

    public Set<DiagramComponent> coreComponents() {
        return this.coreComponents;
    }

    public Set<DiagramComponent> contextComponents() {
        return this.contextComponents;
    }

    public Set<ComponentRelation> coreRelations() {
        return this.coreRelations;
    }

    public Set<ComponentRelation> contextRelations() {
        return this.contextRelations;
    }

    public Set<ComponentRelation> allRelations() {
        return Sets.union(this.contextRelations, this.coreRelations);
    }
}
