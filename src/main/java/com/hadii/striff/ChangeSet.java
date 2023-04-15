package com.hadii.striff;

import com.hadii.striff.diagram.DiagramComponent;
import com.hadii.striff.diagram.StriffCodeModel;
import com.hadii.striff.extractor.ExtractedRelationships;
import com.hadii.striff.extractor.RelationsMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Represents the differences between a primary and secondary code base.
 */
public final class ChangeSet {

    private final RelationsMap deletedRelations = new RelationsMap();
    private final RelationsMap addedRelations = new RelationsMap();
    private final Set<DiagramComponent> addedComponents;
    private final Set<DiagramComponent> deletedComponents;
    private final Set<DiagramComponent> keyRelationsComponents = new HashSet<>();
    private final Set<DiagramComponent> modifiedComponents;
    private static final Logger LOGGER = LogManager.getLogger(ChangeSet.class);

    /**
     * A representation of the changes between an original and final code base.
     * @param oldModel The original source code model
     * @param newModel The final source code model
     */
    public ChangeSet(StriffCodeModel oldModel, StriffCodeModel newModel) {
        LOGGER.info("Generating changeset between old and new code models..");
        RelationsMap oldExtractedRels = new ExtractedRelationships(oldModel).result();
        RelationsMap newExtractedRels = new ExtractedRelationships(newModel).result();

        // Form a list of all newly created components.
        this.addedComponents = new HashSet<>();
        for (final Map.Entry<String, DiagramComponent> entry : newModel.components().entrySet()) {
            if (!oldModel.containsComponent(entry.getKey())) {
                this.addedComponents.add(entry.getValue());
            }
        }
        LOGGER.info("Found " + this.addedComponents.size() + " added components.");

        // Form a list of all deleted components.
        this.deletedComponents = new HashSet<>();
        for (final Map.Entry<String, DiagramComponent> entry : oldModel.components().entrySet()) {
            if (!newModel.containsComponent(entry.getKey())) {
                this.deletedComponents.add(entry.getValue());
            }
        }
        LOGGER.info("Found " + this.deletedComponents.size() + " deleted components.");

        // Form a list of all the new component relationships.
        newExtractedRels.allRels().forEach(relation -> {
            if (!oldExtractedRels.contains(relation)) {
                this.addedRelations.insertRelation(relation);
                this.addKeyRelComponents(relation.originalComponent(), relation.targetComponent());
            }
        });
        LOGGER.info("Found " + this.addedRelations.size() + " added relations.");

        // Form a list of all relationships that do not exist anymore.
        oldExtractedRels.allRels().forEach(relation -> {
            if (!newExtractedRels.contains(relation)) {
                this.deletedRelations.insertRelation(relation);
                this.addKeyRelComponents(relation.originalComponent(), relation.targetComponent());
            }
        });
        LOGGER.info("Found " + this.deletedRelations.size() + " deleted relations.");

        // Form a list of all components whose implementations have changed.
        this.modifiedComponents = new HashSet<>();
        for (final Map.Entry<String, DiagramComponent> entry : newModel.components().entrySet()) {
            if (oldModel.containsComponent(entry.getKey())) {
                if (entry.getValue().componentHashCode() != oldModel.component(entry.getKey()).componentHashCode()) {
                    this.modifiedComponents.add(entry.getValue());
                }
            }
        }
        LOGGER.info("Found " + this.modifiedComponents.size() + " modified components.");
    }

    private void addKeyRelComponents(DiagramComponent... keyRelCmps) {
        for (DiagramComponent keyRelCmp : keyRelCmps) {
            if (!this.addedComponents.contains(keyRelCmp)
                && !this.deletedComponents.contains(keyRelCmp)) {
                this.keyRelationsComponents.add(keyRelCmp);
            }
        }
    }

    public Set<DiagramComponent> addedComponents() {
        return addedComponents;
    }

    public Set<DiagramComponent> deletedComponents() {
        return deletedComponents;
    }

    public Set<DiagramComponent> keyRelationsComponents() {
        return this.keyRelationsComponents;
    }

    public RelationsMap addedRelations() {
        return this.addedRelations;
    }

    public RelationsMap deletedRelations() {
        return this.deletedRelations;
    }

    public Set<DiagramComponent> modifiedComponents() {
        return this.modifiedComponents;
    }
}
