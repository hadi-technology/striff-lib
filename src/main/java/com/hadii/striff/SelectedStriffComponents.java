package com.hadii.striff;

import com.hadii.striff.diagram.DiagramComponent;
import com.hadii.striff.diagram.DiagramConstants;
import com.hadii.striff.extractor.ComponentRelations;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Represents the set of components that are to be displayed in Striff diagram(s).
 */
public class SelectedStriffComponents {

    private final Set<DiagramComponent> allComponents = new HashSet<>();
    private final Set<DiagramComponent> coreComponents = new HashSet<>();

    public SelectedStriffComponents(ComponentRelations relations, ChangeSet changeset) {
        List<DiagramComponent> coreComponents = Stream.of(
                changeset.addedComponents(), changeset.deletedComponents(), changeset.keyContextComponents()
        ).flatMap(Collection::stream).collect(Collectors.toList());
        // Add core "must-draw" components to our list of components to draw
        this.coreComponents.addAll(coreComponents);
        this.allComponents.addAll(this.coreComponents);
        // Add supporting context components to our list of components to draw
        coreComponents.forEach(coreComponent -> {
            relations.relations().forEach(relation -> {
                if (relation.targetComponent().equals(coreComponent) &&
                    relation.associationType() != DiagramConstants.ComponentAssociation.NONE) {
                    allComponents.add(relation.originalComponent());
                }
            });
        });
    }

    public Set<DiagramComponent> allComponents() {
        return this.allComponents;
    }

    public Set<DiagramComponent> coreComponents() {
        return this.coreComponents;
    }
}
