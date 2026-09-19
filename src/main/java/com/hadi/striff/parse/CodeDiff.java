package com.hadi.striff.parse;

import com.hadi.clarpse.sourcemodel.Component;
import com.hadi.clarpse.sourcemodel.OOPSourceCodeModel;
import com.hadi.striff.ChangeSet;
import com.hadi.striff.extractor.ExtractedRelationships;
import com.hadi.striff.extractor.NotLoadedRelation;
import com.hadi.striff.extractor.RelationsMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serial;
import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.CancellationException;
import java.util.stream.Collectors;

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
 *   <li><strong>notLoadedRelations</strong> - Relationships of the merged model to repository
 *       types it holds no component for; only a one-level analysis produces them</li>
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
    private final TreeSet<NotLoadedRelation> notLoadedRelations;
    private static final Logger LOGGER = LoggerFactory.getLogger(CodeDiff.class);

    /**
     * How many components are processed between interrupt checks during the merge.
     *
     * <p>Merging two large models is single-threaded and CPU-bound, and is one of the phases where a
     * caller that enforces a time budget otherwise sits past its interrupt. This mirrors the
     * cooperative-cancellation checkpoint that {@code OOPSourceCodeModel.merge} performs in clarpse:
     * checking every few hundred
     * components keeps cancellation responsive within milliseconds while the overhead is unmeasurable,
     * and the checkpoint is completely inert on any run that is never interrupted.
     */
    private static final int INTERRUPT_CHECK_INTERVAL = 512;

    /**
     * Aborts the merge when the calling thread has been interrupted.
     *
     * <p>Cooperative cancellation: a caller that enforces a time budget interrupts this thread, and the
     * model merge is one of the long CPU-bound phases that would otherwise ignore the request.
     * Re-asserts the interrupt flag before throwing so callers can still observe it, then throws an unchecked
     * {@link CancellationException} that unwinds out of the {@code CodeDiff} constructor so the caller
     * can convert it to a timeout.
     */
    private static void throwIfInterrupted() {
        if (Thread.currentThread().isInterrupted()) {
            Thread.currentThread().interrupt();
            throw new CancellationException("Analysis interrupted while merging code models.");
        }
    }

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
        if (olderModel.size() == 0 && newerModel.size() == 0) {
            LOGGER.info("No components in old or new models, skipping diff/merge/relationship extraction.");
            this.changeSet = new ChangeSet(olderModel, newerModel);
            this.mergedModel = newerModel.copy();
            this.relationsMap = new RelationsMap();
            this.notLoadedRelations = new TreeSet<>();
            return;
        }

        // The change set is computed from the two revisions as parsed, before anything is merged
        // onto either. It used to be handed the copy that the merge then mutated, which was safe
        // only because every component it held was a defensive copy; it holds the models' own
        // components now, so the pristine model is what it must be given.
        this.changeSet = new ChangeSet(olderModel, newerModel);

        OOPSourceCodeModel merged = newerModel.copy();
        LOGGER.info("Merging old and new code models..");
        mergeOldOnlyComponents(olderModel, merged);
        mergeDeletedChildrenOntoSurvivingParents(olderModel, merged);
        this.mergedModel = merged;
        ExtractedRelationships extracted = new ExtractedRelationships(this.mergedModel);
        this.relationsMap = extracted.result();
        this.notLoadedRelations = new TreeSet<>(extracted.notLoadedRelations());
    }

    /**
     * Carries components the change deleted into the merged model, so that a diagram can show what
     * went away beside what arrived.
     */
    private static void mergeOldOnlyComponents(final OOPSourceCodeModel olderModel,
                                               final OOPSourceCodeModel merged) {
        final List<Component> oldComponents = olderModel.components().collect(Collectors.toList());
        throwIfInterrupted();
        int sinceCheck = 0;
        for (final Component oldCmp : oldComponents) {
            if (++sinceCheck >= INTERRUPT_CHECK_INTERVAL) {
                sinceCheck = 0;
                throwIfInterrupted();
            }
            if (!merged.containsComponent(oldCmp.uniqueName())) {
                merged.insertComponent(oldCmp);
            }
        }
    }

    /**
     * Lists a deleted member among the children of the parent that survived it.
     *
     * <p><b>This branch of the merge had no effect at all before, and its absence was visible to a
     * reader.</b> It read the surviving parent through {@code copyOfComponent}, which deep-copies, and
     * so inserted the missing children into a copy that was then discarded -- leaving the merged
     * parent listing only the members the head revision still declares. A class whose method was
     * deleted therefore rendered as though nothing had been removed from it, and
     * {@code PUMLClassFieldsCode.generateChangeSummary} counts deleted children by walking exactly
     * this list, so its deleted-member count was structurally incapable of being anything but zero.
     * The deleted component itself was in the merged model the whole time; only its parent's account
     * of it was missing.
     *
     * <p>It runs after {@link #mergeOldOnlyComponents} rather than in the same pass, so that the
     * merged model is complete and a child can be dropped when it names no component in either
     * revision. Callers dereference these names against the merged model without checking.
     */
    private static void mergeDeletedChildrenOntoSurvivingParents(final OOPSourceCodeModel olderModel,
                                                                 final OOPSourceCodeModel merged) {
        final List<Component> oldComponents = olderModel.components().collect(Collectors.toList());
        throwIfInterrupted();
        int sinceCheck = 0;
        for (final Component oldCmp : oldComponents) {
            if (++sinceCheck >= INTERRUPT_CHECK_INTERVAL) {
                sinceCheck = 0;
                throwIfInterrupted();
            }
            if (oldCmp.children().isEmpty()) {
                continue;
            }
            merged.component(oldCmp.uniqueName()).ifPresent(mergedCmp -> {
                // Hoisted, and a set: this was a fresh copy of the list per child, with a linear
                // scan of it on top, on a model of twelve thousand components.
                final Set<String> alreadyListed = new HashSet<>(mergedCmp.children());
                oldCmp.children().stream()
                        .filter(child -> !alreadyListed.contains(child))
                        .filter(merged::containsComponent)
                        .forEach(mergedCmp::insertChildComponent);
            });
        }
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
     * Returns the merged model's relationships to repository types it holds no component for.
     *
     * <p>Only a one-level analysis produces them: a boundary component's references past the
     * boundary, and references into files a budget held back. They are never drawn. Each records
     * whether it starts from a component analysed in full or from a boundary component.</p>
     *
     * @return the not-loaded relationships, in a stable order; empty for an ordinary analysis
     */
    public Set<NotLoadedRelation> notLoadedRelations() {
        // Null only in an instance serialised before the field existed.
        if (this.notLoadedRelations == null) {
            return Collections.emptySet();
        }
        return Collections.unmodifiableSet(this.notLoadedRelations);
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
