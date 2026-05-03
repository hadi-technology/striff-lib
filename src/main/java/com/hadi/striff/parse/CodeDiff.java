package com.hadi.striff.parse;

import com.hadi.clarpse.sourcemodel.OOPSourceCodeModel;
import com.hadi.striff.ChangeSet;
import com.hadi.striff.extractor.ExtractedRelationships;
import com.hadi.striff.extractor.RelationsMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serial;
import java.io.Serializable;

/**
 * Represents the product of merging and comparing two code models.
 *
 * <p>This class is the core intermediate representation between parsing and
 * rendering. It contains:</p>
 * <ul>
 *   <li><strong>mergedModel</strong> - Combined old + new components (old-only
 *       components are preserved for context)</li>
 *   <li><strong>oldModel</strong> - Snapshot of the original codebase</li>
 *   <li><strong>newModel</strong> - Snapshot of the updated codebase</li>
 *   <li><strong>changeSet</strong> - Computed differences (added/deleted/modified
 *       components and relations)</li>
 *   <li><strong>relationsMap</strong> - All relationships extracted from the
 *       merged model</li>
 * </ul>
 *
 * <h2>Key Optimization: Single Extraction</h2>
 * <p>Relationships are extracted <strong>once</strong> from the merged model,
 * then filtered by component names to obtain old/new relations. This avoids
 * the redundant extractions that would occur if extracting separately from
 * old and new models.</p>
 *
 * <h2>Usage in Render-Only Mode</h2>
 * <p>This class can be passed to the render-only {@link com.hadi.striff.StriffOperation}
 * constructor to generate additional diagrams without re-parsing source files.</p>
 *
 * <h3>Short-Circuit Optimization</h3>
 * <p>If both input models are empty, relationship extraction is skipped
 * entirely, returning an empty relations map and change set.</p>
 */
public class CodeDiff implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private final OOPSourceCodeModel mergedModel;
    private final OOPSourceCodeModel oldModel;
    private final OOPSourceCodeModel newModel;
    private final ChangeSet changeSet;
    private final RelationsMap relationsMap;
    private static final Logger LOGGER = LoggerFactory.getLogger(CodeDiff.class);

    /**
     * Merges the newer source code model onto the older model and computes differences.
     *
     * <p>The merge process preserves components from the old model that are not
     * present in the new model, ensuring that deleted components remain visible
     * in the merged model for context. Relationships are extracted once from the
     * merged model, then filtered to compute the change set.</p>
     *
     * <p><strong>Short-circuit:</strong> If both models are empty, returns an
     * empty CodeDiff without performing extraction.</p>
     *
     * @param olderModel the original code model (before changes)
     * @param newerModel the updated code model (after changes)
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

    /**
     * Returns the merged model containing all components from both old and new models.
     *
     * <p>The merged model preserves old-only components for context, enabling
     * diagrams to show what was deleted alongside what was added.</p>
     *
     * @return the merged code model
     */
    public OOPSourceCodeModel mergedModel() {
        return this.mergedModel;
    }

    /**
     * Returns all relationships extracted from the merged model.
     *
     * <p>This map can be filtered by component names to obtain subsets of
     * relations for specific components.</p>
     *
     * @return the complete relations map
     */
    public RelationsMap extractedRels() {
        return this.relationsMap;
    }

    /**
     * Returns the computed change set between the old and new models.
     *
     * @return the change set containing added/deleted/modified components and relations
     */
    public ChangeSet changeSet() {
        return this.changeSet;
    }

    /**
     * Returns the original (older) code model.
     *
     * @return the old code model snapshot
     */
    public OOPSourceCodeModel oldModel() {
        return oldModel;
    }

    /**
     * Returns the updated (newer) code model.
     *
     * @return the new code model snapshot
     */
    public OOPSourceCodeModel newModel() {
        return newModel;
    }
}
