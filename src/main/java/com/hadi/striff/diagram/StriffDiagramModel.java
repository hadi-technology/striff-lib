package com.hadi.striff.diagram;

import com.hadi.clarpse.sourcemodel.Component;
import com.hadi.striff.ChangeSet;
import com.hadi.striff.annotations.LogExecutionTime;
import com.hadi.striff.extractor.RelationsMap;
import com.hadi.striff.parse.CodeDiff;
import com.hadi.striff.spi.DiagramAugmenter;
import com.hadi.striff.spi.SpiLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Represents the set of components and relations that are to be displayed in a
 * Striff diagram.
 */

public class StriffDiagramModel {

    private final Set<DiagramComponent> diagramCmps = new HashSet<>();
    private RelationsMap diagramRels = new RelationsMap();
    private static final Logger LOGGER = LoggerFactory.getLogger(StriffDiagramModel.class);

    public StriffDiagramModel(CodeDiff codeDiff) {
        this(codeDiff, Collections.emptySet());
    }

    @LogExecutionTime
    public StriffDiagramModel(CodeDiff codeDiff, Set<String> sourceFilesFilter) {
        this(codeDiff, sourceFilesFilter, true);
    }

    @LogExecutionTime
    public StriffDiagramModel(CodeDiff codeDiff, Set<String> sourceFilesFilter, boolean enableAugmenters) {
        LOGGER.info("Generating diagram model..");
        Set<String> targetCmpNames = codeDiff.mergedModel().components()
                .filter(cmp -> sourceFilesFilter.contains(cmp.sourceFile())).map(Component::uniqueName)
                .collect(Collectors.toSet());
        LOGGER.debug("The following components will be analyzed: {}", targetCmpNames);
        getCoreBaseCmps(codeDiff, sourceFilesFilter).forEach(
                cmpName -> this.diagramCmps.add(new DiagramComponent(
                        cmpName, codeDiff.mergedModel())));
        if (enableAugmenters) {
            applyAugmenters(codeDiff);
        }
        getCoreRelations(this.diagramCmps, codeDiff.extractedRels());
    }

    private void getCoreRelations(Set<DiagramComponent> diagramCmps, RelationsMap extractedRels) {
        LOGGER.info("Selecting diagram relationships...");
        this.diagramRels = extractedRels
                .filteredRelations(diagramCmps.stream().map(DiagramComponent::uniqueName).collect(Collectors.toSet()));
        LOGGER.info(this.diagramRels.size() + " relations will be displayed.");
    }

    private Set<String> getCoreBaseCmps(CodeDiff codeDiff, Set<String> sourceFilesFilter) {
        LOGGER.info("Selecting diagram components...");
        Set<String> diagramCmpNames = new HashSet<>();
        ChangeSet changeSet = codeDiff.changeSet();

        // Start with added/deleted/modified components
        Set<String> filteredCmps = new HashSet<>();
        if (sourceFilesFilter.isEmpty()) {
            // No filter: include all added/deleted/modified/keyRel components
            filteredCmps = Stream.of(changeSet.addedComponents(),
                    changeSet.deletedComponents(),
                    changeSet.keyRelationsComponents(),
                    changeSet.modifiedComponents()).flatMap(Collection::stream).collect(Collectors.toSet());
        } else {
            // With filter: include added/deleted/modified components from filtered files only
            filteredCmps = Stream.of(changeSet.addedComponents(),
                    changeSet.deletedComponents(),
                    changeSet.modifiedComponents()).flatMap(Collection::stream)
                    .filter(cmp -> {
                        var cmpOpt = codeDiff.mergedModel().getComponent(cmp);
                        return cmpOpt.isPresent() && sourceFilesFilter.contains(cmpOpt.get().sourceFile());
                    })
                    .collect(Collectors.toSet());
        }

        // Add ALL components related to the filtered components (as contextual gray components)
        // This includes both new and existing relations
        Set<String> contextualCmps = new HashSet<>();
        for (String cmpName : filteredCmps) {
            var cmpOpt = codeDiff.mergedModel().getComponent(cmpName);
            if (cmpOpt.isEmpty()) {
                continue;
            }
            Component cmp = cmpOpt.get();
            // Find all components that this component relates to (outgoing relations)
            var relations = codeDiff.extractedRels().rels(cmp);
            for (var relation : relations) {
                contextualCmps.add(relation.targetComponent().uniqueName());
            }
            // Find all components that relate to this component (incoming relations)
            for (var relation : codeDiff.extractedRels().allRels()) {
                if (relation.targetComponent().uniqueName().equals(cmpName)) {
                    contextualCmps.add(relation.originalComponent().uniqueName());
                }
            }
        }

        // Combine filtered and contextual components
        Set<String> unfilteredCoreCmps = new HashSet<>();
        unfilteredCoreCmps.addAll(filteredCmps);
        unfilteredCoreCmps.addAll(contextualCmps);

        unfilteredCoreCmps.forEach(diagramComponent -> {
            var cmpOpt = codeDiff.mergedModel().getComponent(diagramComponent);
            if (cmpOpt.isEmpty()) {
                LOGGER.debug("Component {} not found in merged model, skipping", diagramComponent);
                return;
            }
            Component cmp = cmpOpt.get();
            if (cmp.componentType().isBaseComponent()) {
                diagramCmpNames.add(diagramComponent);
            } else {
                try {
                    Component parentBaseCmp = codeDiff.mergedModel().parentBaseCmp(diagramComponent);
                    if (parentBaseCmp != null) {
                        diagramCmpNames.add(parentBaseCmp.uniqueName());
                    }
                } catch (IllegalArgumentException e) {
                    // Component has no parent (e.g., module-level function/field)
                    // Skip it as it will be handled by synthetic module support
                    LOGGER.debug("Skipping component with no parent: {}", diagramComponent);
                }
            }
        });
        LOGGER.info(diagramCmpNames.size() + " components will be displayed.");
        return diagramCmpNames;
    }

    public Set<DiagramComponent> allBaseCmps() {
        return this.diagramCmps.stream().filter(cmp -> cmp.componentType().isBaseComponent())
                .collect(Collectors.toSet());
    }

    public Set<DiagramComponent> diagramCmps() {
        return this.diagramCmps;
    }

    public RelationsMap diagramRels() {
        return this.diagramRels;
    }

    public boolean empty() {
        return this.diagramCmps.isEmpty();
    }

    private void applyAugmenters(CodeDiff codeDiff) {
        for (DiagramAugmenter augmenter : SpiLoader.loadOrdered(DiagramAugmenter.class, DiagramAugmenter::order)) {
            augmenter.augment(codeDiff, this.diagramCmps);
        }
    }
}
