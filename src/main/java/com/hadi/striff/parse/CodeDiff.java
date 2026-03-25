package com.hadi.striff.parse;

import com.hadi.clarpse.sourcemodel.OOPSourceCodeModel;
import com.hadi.striff.ChangeSet;
import com.hadi.striff.extractor.ExtractedRelationships;
import com.hadi.striff.extractor.RelationsMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Represents the product of merging multiple {@link OOPSourceCodeModel} together.
 */
public class CodeDiff {

    private final OOPSourceCodeModel mergedModel;
    private final OOPSourceCodeModel oldModel;
    private final OOPSourceCodeModel newModel;
    private final ChangeSet changeSet;
    private final RelationsMap relationsMap;
    private static final Logger LOGGER = LogManager.getLogger(CodeDiff.class);

    /**
     * Merges the newer source code mergedModel onto the older source code
     * mergedModel.
     */
    public CodeDiff(OOPSourceCodeModel olderModel, OOPSourceCodeModel newerModel) {
        this.oldModel = olderModel;
        this.newModel = newerModel;

        // Short-circuit: if no components in either model, skip processing
        boolean hasOldComponents = olderModel.components().count() > 0;
        boolean hasNewComponents = newerModel.components().count() > 0;

        if (!hasOldComponents && !hasNewComponents) {
            LOGGER.info("No components in old or new models, skipping diff/merge/relationship extraction.");
            this.changeSet = new ChangeSet(olderModel, newerModel.copy());
            this.mergedModel = newerModel.copy();
            this.relationsMap = new RelationsMap();
            return;
        }

        OOPSourceCodeModel newerModelCopy = newerModel.copy();
        this.changeSet = new ChangeSet(olderModel, newerModelCopy);
        // Inefficient way to merge the given sets of components..
        LOGGER.info("Merging old and new code models..");
        olderModel.components().forEach(oldCmp -> {
            newerModelCopy.getComponent(oldCmp.uniqueName()).ifPresentOrElse(
                    newCmp -> oldCmp.children().stream()
                            .filter(child -> !newCmp.children().contains(child))
                            .forEach(newCmp::insertChildComponent),
                    () -> newerModelCopy.insertComponent(oldCmp));
        });
        this.mergedModel = newerModelCopy;
        this.relationsMap = new ExtractedRelationships(this.mergedModel).result();
    }

    public OOPSourceCodeModel mergedModel() {
        return this.mergedModel;
    }

    public RelationsMap extractedRels() {
        return this.relationsMap;
    }

    public ChangeSet changeSet() {
        return this.changeSet;
    }

    public OOPSourceCodeModel oldModel() {
        return oldModel;
    }

    public OOPSourceCodeModel newModel() {
        return newModel;
    }
}
