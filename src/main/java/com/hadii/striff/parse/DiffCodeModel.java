package com.hadii.striff.parse;

import com.hadii.striff.ChangeSet;
import com.hadii.striff.diagram.DiagramComponent;
import com.hadii.striff.diagram.DiagramCodeModel;
import com.hadii.striff.extractor.ComponentRelations;

/**
 * Represents the product of merging multiple {@link DiagramCodeModel} together.
 */
public class DiffCodeModel {

    private final DiagramCodeModel mergedSourceCodeModel;
    private final ChangeSet changeSet;
    private final ComponentRelations relations;
    /**
     * Merges the newer source code mergedModel onto the older source code mergedModel.
     */
    public DiffCodeModel(DiagramCodeModel olderModel, DiagramCodeModel newerModel) {
        DiagramCodeModel newerModelCopy = newerModel.copy();
        this.changeSet = new ChangeSet(olderModel, newerModelCopy);
        // Inefficient way to merge the given sets of components..
        for (DiagramComponent oldCmp : olderModel.components().values()) {
            boolean existsInNewerSet = false;
            if (newerModelCopy.containsComponent(oldCmp.uniqueName())) {
                DiagramComponent newCmp = newerModelCopy.component(oldCmp.uniqueName());
                    existsInNewerSet = true;
                    // merge the old components children into the newer set
                    for (String olderCmpChild : oldCmp.children()) {
                        if (!newCmp.children().contains(olderCmpChild)) {
                            newCmp.children().add(olderCmpChild);
                        }
                    }
            }
            if (!existsInNewerSet) {
                newerModelCopy.addComponent(oldCmp);
            }
        }
        this.mergedSourceCodeModel = newerModelCopy;
        this.relations = new ComponentRelations(this.mergedModel());
    }

    public DiagramCodeModel mergedModel() {
        return this.mergedSourceCodeModel;
    }

    public ComponentRelations relations() {
        return this.relations;
    }

    public ChangeSet changeSet() {
        return this.changeSet;
    }
}
