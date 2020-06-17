package com.hadii.stiff;

import com.hadii.stiff.diagram.DiagramComponent;
import com.hadii.stiff.diagram.DiagramCodeModel;
import com.hadii.stiff.extractor.ComponentRelations;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Represents the differences between a primary and secondary code base.
 */
public final class ChangeSet {

    private final ComponentRelations deletedRelations = new ComponentRelations();
    private final ComponentRelations addedRelations = new ComponentRelations();
    private final HashSet<DiagramComponent> addedComponents;
    private final Set<DiagramComponent> deletedComponents;
    private final List<DiagramComponent> keyRelationsComponents = new ArrayList<>();

    /**
     * A representation of the changes between an original and final code base.
     * @param oldModel The original source code model
     * @param newModel The final source code model
     */
    public ChangeSet(DiagramCodeModel oldModel, DiagramCodeModel newModel) {
        ComponentRelations oldComponentRelations = new ComponentRelations(oldModel);
        ComponentRelations newComponentRelations = new ComponentRelations(newModel);

        // Form a list of all components that exist in the newer code base but not in the older code base.
        this.addedComponents = new HashSet<>();
        for (final Map.Entry<String, DiagramComponent> entry : newModel.components().entrySet()) {
            if (!oldModel.containsComponent(entry.getKey())) {
                this.addedComponents.add(entry.getValue());
            }
        }
        // Form a list of all components that do not exist in the newer code base but do exist in the older code base.
        this.deletedComponents = new HashSet<>();
        for (final Map.Entry<String, DiagramComponent> entry : oldModel.components().entrySet()) {
            if (!newModel.containsComponent(entry.getKey())) {
                this.deletedComponents.add(entry.getValue());
            }
        }

        // Form a list of all component relationships that exist in the newer code base but not in the older code base.
        newComponentRelations.relations().forEach(relation -> {
            if (!oldComponentRelations.hasRelation(relation)) {
                this.addedRelations.addRelation(relation);
                if (relation.associationType().strength() > 0) {
                    this.addKeyRelationsComponent(relation.originalComponent());
                    this.addKeyRelationsComponent(relation.targetComponent());
                }
            }
        });

        // Form a list of all component relationships that exist in the old code base but not in the new code base.
        oldComponentRelations.relations().forEach(relation -> {
        if (!newComponentRelations.hasRelation(relation)) {
            this.deletedRelations.addRelation(relation);
                this.addKeyRelationsComponent(relation.originalComponent());
                this.addKeyRelationsComponent(relation.targetComponent());
            }
        });
    }

    private void addKeyRelationsComponent(DiagramComponent contextComponent) {
        if (!this.addedComponents.contains(contextComponent) && !this.deletedComponents.contains(contextComponent)) {
            this.keyRelationsComponents.add(contextComponent);
        }
    }

    public Set<DiagramComponent> addedComponents() {
        return addedComponents;
    }

    public Set<DiagramComponent> deletedComponents() {
        return deletedComponents;
    }

    public List<DiagramComponent> keyRelationsComponents() {
        return this.keyRelationsComponents;
    }

    public ComponentRelations addedRelations() {
        return this.addedRelations;
    }

    public ComponentRelations deletedRelations() {
        return this.deletedRelations;
    }
}
