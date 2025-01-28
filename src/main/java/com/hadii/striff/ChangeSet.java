package com.hadii.striff;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.hadii.clarpse.sourcemodel.Component;
import com.hadii.clarpse.sourcemodel.OOPSourceCodeModel;
import com.hadii.striff.extractor.ExtractedRelationships;
import com.hadii.striff.extractor.RelationsMap;

import java.util.HashSet;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 * Represents the differences between a primary and secondary code base.
 */
public final class ChangeSet {

    @JsonIgnore
    private static final Logger LOGGER = LogManager.getLogger(ChangeSet.class);

    private final RelationsMap deletedRelations = new RelationsMap();
    private final RelationsMap addedRelations = new RelationsMap();
    private final Set<String> addedComponents = new HashSet<>();
    private final Set<String> deletedComponents = new HashSet<>();
    private final Set<String> keyRelationsComponents = new HashSet<>();
    private final Set<String> modifiedComponents = new HashSet<>();

    public ChangeSet(OOPSourceCodeModel oldModel, OOPSourceCodeModel newModel) {
        LOGGER.info("Generating changeset between old and new code models..");
        RelationsMap oldExtractedRels = new ExtractedRelationships(oldModel).result();
        RelationsMap newExtractedRels = new ExtractedRelationships(newModel).result();

        // List of newly created components
        newModel.components()
                .filter(cmp -> !oldModel.containsComponent(cmp.uniqueName()))
                .forEach(cmp -> this.addedComponents.add(cmp.uniqueName()));
        LOGGER.info("Found " + this.addedComponents.size() + " added components.");

        // List of deleted components
        oldModel.components()
                .filter(cmp -> !newModel.containsComponent(cmp.uniqueName()))
                .forEach(cmp -> this.deletedComponents.add(cmp.uniqueName()));
        LOGGER.info("Found " + this.deletedComponents.size() + " deleted components.");

        // New relationships
        newExtractedRels.allRels().forEach(relation -> {
            if (!oldExtractedRels.contains(relation)) {
                this.addedRelations.insertRelation(relation);
                this.addKeyRelComponents(relation.originalComponent(), relation.targetComponent());
            }
        });
        LOGGER.info("Found " + this.addedRelations.size() + " added relations.");

        // Deleted relationships
        oldExtractedRels.allRels().forEach(relation -> {
            if (!newExtractedRels.contains(relation)) {
                this.deletedRelations.insertRelation(relation);
                this.addKeyRelComponents(relation.originalComponent(), relation.targetComponent());
            }
        });
        LOGGER.info("Found " + this.deletedRelations.size() + " deleted relations.");

        // Modified components
        newModel.components().filter(cmp -> oldModel.containsComponent(cmp.uniqueName()))
            .forEach(cmp -> {
                Component oldCmp = oldModel.getComponent(cmp.uniqueName()).orElse(null);
                if (oldCmp != null && cmp.hashCode() != oldCmp.hashCode()) {
                    this.modifiedComponents.add(cmp.uniqueName());
                }
            });
        LOGGER.info("Found " + this.modifiedComponents.size() + " modified components.");
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
