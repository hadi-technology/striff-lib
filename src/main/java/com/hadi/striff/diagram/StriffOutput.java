package com.hadi.striff.diagram;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.hadi.clarpse.compiler.CompileFailure;
import com.hadi.striff.StriffConfig;
import com.hadi.striff.diagram.display.DiagramDisplay;
import com.hadi.striff.diagram.display.OutputMode;
import com.hadi.striff.diagram.display.PartitionPlacement;
import com.hadi.striff.diagram.partition.PackagePartitionStrategy;
import com.hadi.striff.diagram.partition.PartitionStrategy;
import com.hadi.striff.diagram.plantuml.PUMLDrawException;
import com.hadi.striff.extractor.RelationsMap;
import com.hadi.striff.parse.CodeDiff;
import com.hadi.striff.spi.DiagramDisplayOverlay;
import com.hadi.striff.spi.SpiLoader;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class StriffOutput {

    @JsonIgnore
    private static final Logger LOGGER = LoggerFactory.getLogger(StriffOutput.class);

    private final Set<String> compileWarnings = new HashSet<>();
    private final List<StriffDiagram> diagrams = new ArrayList<>();

    public StriffOutput(CodeDiff codeDiff, StriffConfig config) throws PUMLDrawException, IOException {
        this(codeDiff, config, Collections.emptySet());
    }

    /**
     * Creates diagram output and compile warnings for the supplied code diff.
     *
     * @param codeDiff parsed differences between the original and updated models
     * @param config generation configuration
     * @param compileFailures compile warnings gathered during parsing
     * @throws PUMLDrawException if PlantUML rendering fails
     * @throws IOException if diagram serialization fails
     */
    public StriffOutput(CodeDiff codeDiff, StriffConfig config, Set<CompileFailure> compileFailures)
            throws PUMLDrawException, IOException {
        // Short-circuit: if no components in either model, skip all processing
        boolean hasOldComponents = codeDiff.oldModel().components().count() > 0;
        boolean hasNewComponents = codeDiff.newModel().components().count() > 0;

        if (!hasOldComponents && !hasNewComponents) {
            LOGGER.info("No components found in old or new models, skipping diagram generation.");
            if (config.filesFilter().isEmpty()) {
                compileFailures.forEach(failure -> this.compileWarnings.add(failure.file().path()));
            } else {
                compileFailures.stream()
                        .filter(failure -> config.filesFilter().contains(failure.file().path()))
                        .forEach(failure -> this.compileWarnings.add(failure.file().path()));
            }
            return;
        }

        StriffDiagramModel sDM = new StriffDiagramModel(codeDiff, config.filesFilter(), config.enableAugmenters());
        generateDiagrams(codeDiff, sDM.diagramRels(), partitionConfig(sDM, config), config);
        if (config.filesFilter().isEmpty()) {
            compileFailures.forEach(failure -> this.compileWarnings.add(failure.file().path()));
        } else {
            compileFailures.stream()
                    .filter(failure -> config.filesFilter().contains(failure.file().path()))
                    .forEach(failure -> this.compileWarnings.add(failure.file().path()));
        }
    }

    private Pair<PartitionStrategy, PartitionPlacement> partitionConfig(
            StriffDiagramModel striffDiagramModel, StriffConfig config) {
        PartitionStrategy strategy;
        PartitionPlacement placementStrat;

        if (config.outputMode() == OutputMode.DEFAULT) {
            strategy = new PackagePartitionStrategy(striffDiagramModel);
            placementStrat = PartitionPlacement.CONDENSED;
        } else {
            throw new IllegalArgumentException("Output mode " + config.outputMode().name() + " is not supported!");
        }
        return Pair.of(strategy, placementStrat);
    }

    private void generateDiagrams(CodeDiff codeDiff,
            RelationsMap diagramRels, Pair<PartitionStrategy, PartitionPlacement> partitionConf,
            StriffConfig config) throws IOException, PUMLDrawException {

        LOGGER.info("Generating diagram with partition conf: {}", partitionConf);

        PartitionPlacement placementStrategy = partitionConf.getRight();
        PartitionStrategy partitionStrategy = partitionConf.getLeft();

        List<Set<DiagramComponent>> cmpPartitions = partitionStrategy.apply();
        LOGGER.info("{} partitions were generated.", cmpPartitions.size());

        if (placementStrategy == PartitionPlacement.ONE_PER_DIAGRAM) {
            for (Set<DiagramComponent> currPartition : cmpPartitions) {
                this.insertDiagram(
                        new StriffDiagram(codeDiff, currPartition, diagramRels,
                                resolveDiagramDisplay(config, this.cmpPkgs(currPartition), currPartition),
                                config));
            }
        } else if (placementStrategy == PartitionPlacement.CONDENSED) {
            Set<DiagramComponent> condensedPartition = cmpPartitions.stream().flatMap(Set::stream)
                    .collect(Collectors.toSet());

            this.insertDiagram(
                    new StriffDiagram(codeDiff, condensedPartition, diagramRels,
                            resolveDiagramDisplay(config, this.cmpPkgs(condensedPartition), condensedPartition), config));
        } else {
            throw new IllegalArgumentException("Placement strategy " + placementStrategy + " is not supported!");
        }

        LOGGER.info("{} StriffDiagram objects were generated.", this.diagrams.size());
    }

    private Set<String> cmpPkgs(Set<DiagramComponent> cmps) {
        return cmps.stream()
                .map(cmp -> ComponentHelper.packagePath(cmp.pkg()))
                .collect(Collectors.toSet());
    }

    private void insertDiagram(StriffDiagram striffDiagram) {
        if (striffDiagram.size() > 0) {
            this.diagrams.add(striffDiagram);
        }
    }

    private DiagramDisplay resolveDiagramDisplay(StriffConfig config, Set<String> pkgs, Set<DiagramComponent> components) {
        DiagramDisplay baseDisplay = new DiagramDisplay(config.colorScheme(), pkgs);
        DiagramDisplay display = baseDisplay.merge(config.displayOverride());
        for (DiagramDisplayOverlay overlay : SpiLoader.loadOrdered(
                DiagramDisplayOverlay.class, DiagramDisplayOverlay::order)) {
            DiagramDisplay overlaid = overlay.overlay(display, components);
            if (overlaid != null) {
                display = overlaid;
            }
        }
        return display;
    }

    /**
     * Returns generated diagrams. The list is never {@code null}; it may be empty
     * when no renderable components were produced.
     *
     * @return generated Striff diagrams
     */
    @JsonProperty("diagrams")
    public List<StriffDiagram> diagrams() {
        return this.diagrams;
    }

    /**
     * Returns compile warnings collected during parsing. The set is never
     * {@code null}; it may be empty when no warnings were produced.
     *
     * @return compile warning file paths
     */
    @JsonProperty("compileWarnings")
    public Set<String> compileWarnings() {
        return this.compileWarnings;
    }
}
