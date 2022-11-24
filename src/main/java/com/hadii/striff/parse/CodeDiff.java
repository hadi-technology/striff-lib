package com.hadii.striff.parse;

import com.hadii.striff.ChangeSet;
import com.hadii.striff.diagram.DiagramComponent;
import com.hadii.striff.diagram.StriffCodeModel;
import com.hadii.striff.extractor.ExtractedRelationships;
import com.hadii.striff.extractor.RelationsMap;

import java.util.Map;

/**
 * Represents the product of merging multiple {@link StriffCodeModel} together.
 */
public class CodeDiff {

    private final StriffCodeModel mergedModel;
    private final ChangeSet changeSet;
    private final ExtractedRelationships relations;

    /**
     * Merges the newer source code mergedModel onto the older source code mergedModel.
     */
    public CodeDiff(StriffCodeModel olderModel, StriffCodeModel newerModel) {
        StriffCodeModel newerModelCopy = newerModel.copy();
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
        this.mergedModel = newerModelCopy;
        this.relations = new ExtractedRelationships(this.mergedModel);
    }


    public Map<String, DiagramComponent> components() {
        return this.mergedModel.components();
    }

    public RelationsMap extractedRels() {
        return this.relations.result();
    }

    public ChangeSet changeSet() {
        return this.changeSet;
    }
}
