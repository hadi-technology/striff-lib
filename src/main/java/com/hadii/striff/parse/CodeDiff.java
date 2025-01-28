package com.hadii.striff.parse;

import com.hadii.clarpse.sourcemodel.OOPSourceCodeModel;
import com.hadii.striff.ChangeSet;
import com.hadii.striff.extractor.ExtractedRelationships;
import com.hadii.striff.extractor.RelationsMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Represents the product of merging multiple {@link StriffCodeModel} together.
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
        OOPSourceCodeModel newerModelCopy = newerModel.copy();
        this.changeSet = new ChangeSet(olderModel, newerModelCopy);
        // Inefficient way to merge the given sets of components..
        LOGGER.info("Merging old and new code models..");
        olderModel.components().forEach(oldCmp -> {
            newerModelCopy.getComponent(oldCmp.uniqueName()).ifPresentOrElse(
                    newCmp -> oldCmp.children().stream()
                            .filter(child -> !newCmp.children().contains(child))
                            .forEach(newCmp.children()::add),
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
