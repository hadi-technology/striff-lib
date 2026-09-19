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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
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
    private RelationsMap extractedRels = new RelationsMap();
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
        this(codeDiff, sourceFilesFilter, Collections.emptySet(), enableAugmenters);
    }

    @LogExecutionTime
    public StriffDiagramModel(CodeDiff codeDiff, Set<String> sourceFilesFilter, final Set<String> expandedFiles, boolean enableAugmenters) {
        LOGGER.info("Generating diagram model..");
        Set<String> targetCmpNames = codeDiff.mergedModel().components()
                .filter(cmp -> sourceFilesFilter.contains(cmp.sourceFile())).map(Component::uniqueName)
                .collect(Collectors.toSet());
        LOGGER.debug("The following components will be analyzed: {}", targetCmpNames);
        getCoreBaseCmps(codeDiff, sourceFilesFilter, expandedFiles).forEach(
                cmpName -> this.diagramCmps.add(new DiagramComponent(
                        cmpName, codeDiff.mergedModel())));
        if (enableAugmenters) {
            applyAugmenters(codeDiff);
        }
        this.extractedRels = codeDiff.extractedRels();
        getCoreRelations(this.diagramCmps, this.extractedRels);
    }

    /**
     * Removes boundary components until at most {@code maxComponents} remain, or none is left to
     * remove.
     *
     * <p>Only a one-level analysis has boundary components: components modelled because analysed
     * code references them. They are drawn as context, so they go before any other component:
     * first those of context files, the callers a caller asked to show, then the others, each group
     * in unique-name order. Every other component stays, so a diagram still over the limit after
     * this is left to the renderer's own limit. The relations shown are narrowed to what remains.
     *
     * @param maxComponents the most components the diagram should hold
     * @param contextFiles  the context files ({@code StriffConfig.expandedFiles()})
     * @return how many components were removed
     */
    public int trimBoundaryComponents(final int maxComponents, final Set<String> contextFiles) {
        if (this.diagramCmps.size() <= maxComponents) {
            return 0;
        }
        final List<DiagramComponent> removable = new ArrayList<>();
        for (DiagramComponent cmp : this.diagramCmps) {
            if (cmp.boundary()) {
                removable.add(cmp);
            }
        }
        if (removable.isEmpty()) {
            return 0;
        }
        removable.sort(Comparator
                .comparing((DiagramComponent cmp) -> !(cmp.sourceFile() != null && contextFiles.contains(cmp.sourceFile())))
                .thenComparing(DiagramComponent::uniqueName));
        int removed = 0;
        for (DiagramComponent cmp : removable) {
            if (this.diagramCmps.size() <= maxComponents) {
                break;
            }
            this.diagramCmps.remove(cmp);
            removed++;
        }
        LOGGER.info("Removed {} boundary component(s) to keep the diagram within {} components.",
                removed, maxComponents);
        getCoreRelations(this.diagramCmps, this.extractedRels);
        return removed;
    }

    private void getCoreRelations(Set<DiagramComponent> diagramCmps, RelationsMap extractedRels) {
        LOGGER.info("Selecting diagram relationships...");
        this.diagramRels = extractedRels
                .filteredRelations(diagramCmps.stream().map(DiagramComponent::uniqueName).collect(Collectors.toSet()));
        LOGGER.info(this.diagramRels.size() + " relations will be displayed.");
    }

    private Set<String> getCoreBaseCmps(CodeDiff codeDiff, Set<String> sourceFilesFilter, final Set<String> expandedFiles) {
        LOGGER.info("Selecting diagram components...");
        Set<String> diagramCmpNames = new HashSet<>();
        ChangeSet changeSet = codeDiff.changeSet();
        Set<String> unfilteredCoreCmps = Stream.of(changeSet.addedComponents(),
                changeSet.deletedComponents(),
                changeSet.keyRelationsComponents(),
                changeSet.modifiedComponents()).flatMap(Collection::stream).collect(Collectors.toSet());

        // Include components from expandedFiles that aren't already in the change set
        if (!expandedFiles.isEmpty()) {
            codeDiff.mergedModel().components()
                    .filter(cmp -> expandedFiles.contains(cmp.sourceFile()))
                    .map(Component::uniqueName)
                    .forEach(unfilteredCoreCmps::add);
        }

        if (!sourceFilesFilter.isEmpty()) {
            // Filter added/deleted/modified components by source file
            // But keep keyRelationsComponents (contextual components) - they should appear as gray
            Set<String> keyRels = changeSet.keyRelationsComponents();
            unfilteredCoreCmps = unfilteredCoreCmps.stream()
                    .filter(cmp -> {
                        // Key relations components are always included (shown as gray contextual)
                        if (keyRels.contains(cmp)) {
                            return true;
                        }
                        // Expanded files components are always included
                        // The model's own component: one path is read off it and nothing is kept.
                        var cmpOpt = codeDiff.mergedModel().component(cmp);
                        if (cmpOpt.isEmpty()) {
                            return false;
                        }
                        String sourceFile = cmpOpt.get().sourceFile();
                        if (sourceFile != null && expandedFiles.contains(sourceFile)) {
                            return true;
                        }
                        // Other components must be in the source file filter
                        return sourceFilesFilter.contains(sourceFile);
                    })
                    .collect(Collectors.toSet());
        }


        unfilteredCoreCmps.forEach(diagramComponent -> {
            // Read-only throughout this block: a type is read off the component, and the parent
            // walk below reads a name off its result. Neither is retained, and a copy of either
            // would be discarded at the end of the iteration.
            var cmpOpt = codeDiff.mergedModel().component(diagramComponent);
            if (cmpOpt.isEmpty()) {
                LOGGER.debug("Component {} not found in merged model, skipping", diagramComponent);
                return;
            }
            Component cmp = cmpOpt.get();
            if (cmp.componentType().isBaseComponent()) {
                diagramCmpNames.add(diagramComponent);
            } else {
                try {
                    Component parentBase = codeDiff.mergedModel().parentBaseComponent(diagramComponent);
                    if (parentBase != null) {
                        diagramCmpNames.add(parentBase.uniqueName());
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
