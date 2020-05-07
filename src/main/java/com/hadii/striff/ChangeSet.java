package com.hadii.striff;

import com.hadii.clarpse.sourcemodel.OOPSourceModelConstants;
import com.hadii.striff.diagram.DiagramComponent;
import com.hadii.striff.diagram.DiagramSourceCodeModel;
import com.hadii.striff.extractor.ComponentRelations;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Represents the differences between a primary and secondary code base.
 */
public final class ChangeSet {

    private ComponentRelations deletedRelations;
    private ComponentRelations addedRelations;
    private List<DiagramComponent> addedComponents = new ArrayList<DiagramComponent>();
    private List<DiagramComponent> deletedComponents = new ArrayList<DiagramComponent>();
    private List<DiagramComponent> keyContextComponents = new ArrayList<DiagramComponent>();

    /**
     * A representation of the changes between an original and final code base.
     * @param oldModel The original source code model
     * @param newModel The final source code model
     */
    public ChangeSet(DiagramSourceCodeModel oldModel, DiagramSourceCodeModel newModel) {
        ComponentRelations oldComponentRelations = new ComponentRelations(oldModel);
        ComponentRelations newComponentRelations = new ComponentRelations(newModel);

        // Form a list of all components that exist in the newer code base but not in the older code base.
        this.addedComponents = new ArrayList<DiagramComponent>();
        for (final Map.Entry<String, DiagramComponent> entry : newModel.components().entrySet()) {
            if (entry.getValue().componentType() != OOPSourceModelConstants.ComponentType.LOCAL
                    && entry.getValue().componentType() != OOPSourceModelConstants.ComponentType.CONSTRUCTOR
                    && !oldModel.containsComponent(entry.getKey())) {
                addedComponents.add(entry.getValue());
            }
        }

        // Form a list of all components that do not exist in the newer code base but do exist in the older code base.
        this.deletedComponents = new ArrayList<DiagramComponent>();
        for (final Map.Entry<String, DiagramComponent> entry : oldModel.components().entrySet()) {
            if (entry.getValue().componentType() != OOPSourceModelConstants.ComponentType.LOCAL
                    && entry.getValue().componentType() != OOPSourceModelConstants.ComponentType.CONSTRUCTOR
                    && !newModel.containsComponent(entry.getKey())) {
                deletedComponents.add(entry.getValue());
            }
        }

        // Form a list of all component relationships that exist in the newer code base but not in the older code base.
        ComponentRelations addedRelations = new ComponentRelations();
        newComponentRelations.relations().forEach(relation -> {
            if (!oldComponentRelations.hasRelation(relation)) {
                this.addedRelations.addRelation(relation);
                this.addKeyContextComponent(relation.originalComponent());
                this.addKeyContextComponent(relation.targetComponent());
            }
        });

        // Form a list of all component relationships that exist in the old code base but not in the new code base.
        ComponentRelations deletedRelations = new ComponentRelations();
        oldComponentRelations.relations().forEach(relation -> {
            if (!newComponentRelations.hasRelation(relation)) {
                this.deletedRelations.addRelation(relation);
                this.addKeyContextComponent(relation.originalComponent());
                this.addKeyContextComponent(relation.targetComponent());
            }
        });
    }

    private void addKeyContextComponent(DiagramComponent keyComponent) {
        if (!this.addedComponents.contains(keyComponent) && !this.deletedComponents.contains(keyComponent)) {
            this.keyContextComponents.add(keyComponent);
        }
    }

    public List<DiagramComponent> addedComponents() {
        return addedComponents;
    }

    public List<DiagramComponent> deletedComponents() {
        return deletedComponents;
    }

    public List<DiagramComponent> keyContextComponents () {
        return this.keyContextComponents;
    }

    public ComponentRelations addedRelations() {
        return this.addedRelations;
    }

    public ComponentRelations deletedRelations() {
        return this.deletedRelations;
    }
}
