package com.hadii.striff.diagram;

import com.hadii.clarpse.compiler.ProjectFile;
import com.hadii.striff.StriffConfig;
import com.hadii.striff.diagram.display.DiagramDisplay;
import com.hadii.striff.diagram.display.LightDiagramColorScheme;
import com.hadii.striff.diagram.display.OutputMode;
import com.hadii.striff.diagram.display.PartitionPlacement;
import com.hadii.striff.diagram.partition.PackagePartitionStrategy;
import com.hadii.striff.diagram.partition.PartitionStrategy;
import com.hadii.striff.diagram.plantuml.PUMLDrawException;
import com.hadii.striff.parse.CodeDiff;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class StriffOutput {

    private static final Logger LOGGER = LogManager.getLogger(StriffOutput.class);
    private final Set<String> compileWarnings = new HashSet<>();
    private final List<StriffDiagram> diagrams = new ArrayList<>();

    public StriffOutput(CodeDiff codeDiff, StriffConfig config) throws PUMLDrawException,
        IOException {
        this(codeDiff, config, Collections.emptySet());
    }

    public StriffOutput(CodeDiff codeDiff, StriffConfig config, Set<ProjectFile> compileFailures) throws PUMLDrawException, IOException {
        StriffDiagramModel sDM = new StriffDiagramModel(codeDiff, config.filesFilter);
        generateDiagrams(codeDiff, partitionConfig(sDM, config));
        if (config.filesFilter.isEmpty()) {
            // Display all compile warnings
            compileFailures.forEach(failure -> this.compileWarnings.add(failure.path()));
        } else {
            // Display only the warnings related to the filter list.
            compileFailures.stream().filter(failure -> config.filesFilter.contains(failure.path()))
                           .forEach(failure -> this.compileWarnings.add(failure.path()));
        }
    }

    private Pair<PartitionStrategy, PartitionPlacement> partitionConfig(StriffDiagramModel striffDiagramModel, StriffConfig config) {
        PartitionStrategy strategy;
        PartitionPlacement placementStrat;
        if (config.outputMode == OutputMode.DEFAULT) {
            strategy = new PackagePartitionStrategy(striffDiagramModel);
            placementStrat = PartitionPlacement.CONDENSED;
        } else {
            throw new IllegalArgumentException("Output mode " + config.outputMode.name() + " is "
                                                   + "not currently supported!");
        }
        return Pair.of(strategy, placementStrat);
    }

    private void generateDiagrams(CodeDiff mergedModel, Pair<PartitionStrategy,
        PartitionPlacement> partitionConf) throws IOException, PUMLDrawException {
        LOGGER.info("Generating diagram with partition conf: " + partitionConf);
        PartitionPlacement placementStrategy = partitionConf.getRight();
        PartitionStrategy partitionStrategy = partitionConf.getLeft();
        List<Set<DiagramComponent>> cmpPartitions = partitionStrategy.apply();
        LOGGER.info(cmpPartitions.size() + " partitions were generated.");
        if (placementStrategy == PartitionPlacement.ONE_PER_DIAGRAM) {
            for (Set<DiagramComponent> currPartition : cmpPartitions) {
                this.insertDiagram(new StriffDiagram(mergedModel, currPartition,
                                                     new DiagramDisplay(new LightDiagramColorScheme(), this.cmpPkgs(currPartition))));
            }
        } else if (placementStrategy == PartitionPlacement.CONDENSED) {
            // Condense partitions into one diagram.
            Set<DiagramComponent> condensedPartition =
                cmpPartitions.stream().flatMap(Set::stream).collect(Collectors.toSet());
            this.insertDiagram(new StriffDiagram(
                mergedModel, condensedPartition, new DiagramDisplay(new LightDiagramColorScheme(),
                                                                    this.cmpPkgs(condensedPartition))));
        } else {
            throw new IllegalArgumentException("Placement strategy " + placementStrategy + " is "
                                                   + "not supported!");
        }
        LOGGER.info(this.diagrams.size() + " diagrams were generated.");
    }

    private Set<String> cmpPkgs(Set<DiagramComponent> cmps) {
        return cmps.stream().map(DiagramComponent::packagePath).collect(Collectors.toSet());
    }

    public List<StriffDiagram> diagrams() {
        return this.diagrams;
    }

    private void insertDiagram(StriffDiagram striffDiagram) {
        if (striffDiagram.size() > 0) {
            this.diagrams.add(striffDiagram);
        }
    }

    public Set<String> compileWarnings() {
        return this.compileWarnings;
    }
}
