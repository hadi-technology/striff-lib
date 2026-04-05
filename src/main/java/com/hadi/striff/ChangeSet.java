package com.hadi.striff;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.hadi.clarpse.sourcemodel.Component;
import com.hadi.clarpse.sourcemodel.OOPSourceCodeModel;
import com.hadi.striff.extractor.ExtractedRelationships;
import com.hadi.striff.extractor.RelationsMap;

import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Represents the differences between two code bases (old and new).
 *
 * <p>This class computes the structural differences between two
 * {@link com.hadi.clarpse.sourcemodel.OOPSourceCodeModel} instances,
 * identifying:</p>
 * <ul>
 *   <li><strong>Added components</strong> - components present in new but not old</li>
 *   <li><strong>Deleted components</strong> - components present in old but not new</li>
 *   <li><strong>Modified components</strong> - components in both with different codeHash</li>
 *   <li><strong>Added relations</strong> - relationships present in new but not old</li>
 *   <li><strong>Deleted relations</strong> - relationships present in old but not new</li>
 * </ul>
 *
 * <p><strong>Key Relations Components:</strong> Components that participate in
 * added or deleted relations but are not themselves added or deleted. These
 * components are important to include in diagrams as they represent the
 * "connective tissue" between changes.</p>
 *
 * <p><strong>Short-circuit optimization:</strong> If both models are empty,
 * relationship extraction is skipped entirely for performance.</p>
 */
public final class ChangeSet {

    @JsonIgnore
    private static final Logger LOGGER = LoggerFactory.getLogger(ChangeSet.class);

    private final RelationsMap deletedRelations = new RelationsMap();
    private final RelationsMap addedRelations = new RelationsMap();
    private final Set<String> addedComponents = new HashSet<>();
    private final Set<String> deletedComponents = new HashSet<>();
    private final Set<String> keyRelationsComponents = new HashSet<>();
    private final Set<String> modifiedComponents = new HashSet<>();

    /**
     * Computes the difference between two source code models.
     *
     * <p>This constructor extracts relationships from both models and compares
     * them to identify changes. For performance, if both models are empty,
     * relationship extraction is skipped entirely.</p>
     *
     * @param oldModel the original (before) code model
     * @param newModel the updated (after) code model
     */
    public ChangeSet(OOPSourceCodeModel oldModel, OOPSourceCodeModel newModel) {
        LOGGER.info("Generating changeset between old and new code models..");

        // Short-circuit: if no components in either model, skip relationship extraction
        boolean hasOldComponents = oldModel.components().count() > 0;
        boolean hasNewComponents = newModel.components().count() > 0;

        if (!hasOldComponents && !hasNewComponents) {
            LOGGER.info("No components in old or new models, skipping changeset analysis.");
            return;
        }

        RelationsMap oldExtractedRels = new ExtractedRelationships(oldModel).result();
        RelationsMap newExtractedRels = new ExtractedRelationships(newModel).result();

        // List of newly created components
        newModel.components()
                .filter(cmp -> !oldModel.containsComponent(cmp.uniqueName()))
                .forEach(cmp -> this.addedComponents.add(cmp.uniqueName()));
        LOGGER.info("Found {} added components.", this.addedComponents.size());

        // List of deleted components
        oldModel.components()
                .filter(cmp -> !newModel.containsComponent(cmp.uniqueName()))
                .forEach(cmp -> this.deletedComponents.add(cmp.uniqueName()));
        LOGGER.info("Found {} deleted components.", this.deletedComponents.size());

        // New relationships
        newExtractedRels.allRels().forEach(relation -> {
            if (!oldExtractedRels.contains(relation)) {
                this.addedRelations.insertRelation(relation);
                this.addKeyRelComponents(relation.originalComponent(), relation.targetComponent());
            }
        });
        LOGGER.info("Found {} added relations.", this.addedRelations.size());

        // Deleted relationships
        oldExtractedRels.allRels().forEach(relation -> {
            if (!newExtractedRels.contains(relation)) {
                this.deletedRelations.insertRelation(relation);
                this.addKeyRelComponents(relation.originalComponent(), relation.targetComponent());
            }
        });
        LOGGER.info("Found {} deleted relations.", this.deletedRelations.size());

        // Modified components
        newModel.components().filter(cmp -> oldModel.containsComponent(cmp.uniqueName()))
            .forEach(cmp -> {
                Component oldCmp = oldModel.getComponent(cmp.uniqueName()).orElse(null);
                if (oldCmp != null && cmp.codeHash() != oldCmp.codeHash()) {
                    this.modifiedComponents.add(cmp.uniqueName());
                }
            });
        LOGGER.info("Found {} modified components.", this.modifiedComponents.size());
    }

    private void addKeyRelComponents(Component... keyRelCmps) {
        for (Component keyRelCmp : keyRelCmps) {
            if (!this.addedComponents.contains(keyRelCmp.uniqueName())
                    && !this.deletedComponents.contains(keyRelCmp.uniqueName())) {
                this.keyRelationsComponents.add(keyRelCmp.uniqueName());
            }
        }
    }

    @JsonProperty("addedComponents")
    public Set<String> addedComponents() {
        return addedComponents;
    }

    @JsonProperty("deletedComponents")
    public Set<String> deletedComponents() {
        return deletedComponents;
    }

    @JsonProperty("keyRelationsComponents")
    public Set<String> keyRelationsComponents() {
        return keyRelationsComponents;
    }

    @JsonProperty("modifiedComponents")
    public Set<String> modifiedComponents() {
        return modifiedComponents;
    }

    @JsonProperty("addedRelations")
    public RelationsMap addedRelations() {
        return addedRelations;
    }

    @JsonProperty("deletedRelations")
    public RelationsMap deletedRelations() {
        return deletedRelations;
    }

    public boolean inAddedComponents(String cmpUniqueName) {
        return this.addedComponents.contains(cmpUniqueName);
    }

    public boolean inDeletedComponents(String cmpUniqueName) {
        return this.deletedComponents.contains(cmpUniqueName);
    }

    public boolean inKeyRelationComponents(String cmpUniqueName) {
        return this.keyRelationsComponents.contains(cmpUniqueName);
    }
}
