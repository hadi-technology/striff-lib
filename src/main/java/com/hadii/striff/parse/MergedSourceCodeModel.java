package com.hadii.striff.parse;

import com.hadii.striff.diagram.DiagramComponent;
import com.hadii.striff.diagram.DiagramSourceCodeModel;

/**
 * Represents the product of two merged source code models.
 */
public class MergedSourceCodeModel {

    private final DiagramSourceCodeModel mergedSourceCodeModel;

    /**
     * Merges the newer source code model onto the older source code model.
     */
    public MergedSourceCodeModel(DiagramSourceCodeModel olderModel, DiagramSourceCodeModel newerModel) {

        // inefficient way to merge the given sets of components..
        for (DiagramComponent oldCmp : olderModel.components().values()) {
            boolean existsInNewerSet = false;
            if (newerModel.containsComponent(oldCmp.uniqueName())) {
                DiagramComponent newCmp = newerModel.component(oldCmp.uniqueName());
                    existsInNewerSet = true;
                    // merge the old components children into the newer set
                    for (String olderCmpChild : oldCmp.children()) {
                        if (!newCmp.children().contains(olderCmpChild)) {
                            newCmp.children().add(olderCmpChild);
                        }
                    }
            }
            if (!existsInNewerSet) {
                newerModel.addComponent(oldCmp);
            }
        }
        this.mergedSourceCodeModel = newerModel;
    }

    public DiagramSourceCodeModel model() {
        return this.mergedSourceCodeModel;
    }
}
