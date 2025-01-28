package com.hadii.striff.diagram;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.hadii.clarpse.compiler.ProjectFile;
import com.hadii.striff.StriffConfig;
import com.hadii.striff.diagram.display.DiagramDisplay;
import com.hadii.striff.diagram.display.OutputMode;
import com.hadii.striff.diagram.display.PartitionPlacement;
import com.hadii.striff.diagram.partition.PackagePartitionStrategy;
import com.hadii.striff.diagram.partition.PartitionStrategy;
import com.hadii.striff.diagram.plantuml.PUMLDrawException;
import com.hadii.striff.extractor.RelationsMap;
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

    @JsonIgnore
    private static final Logger LOGGER = LogManager.getLogger(StriffOutput.class);

    private final Set<String> compileWarnings = new HashSet<>();
    private final List<StriffDiagram> diagrams = new ArrayList<>();

    public StriffOutput(CodeDiff codeDiff, StriffConfig config) throws PUMLDrawException, IOException {
        this(codeDiff, config, Collections.emptySet());
    }

    public StriffOutput(CodeDiff codeDiff, StriffConfig config, Set<ProjectFile> compileFailures)
            throws PUMLDrawException, IOException {
        StriffDiagramModel sDM = new StriffDiagramModel(codeDiff, config.filesFilter(), config.processMetrics());
        generateDiagrams(codeDiff, sDM.diagramRels(), partitionConfig(sDM, config), config);
        if (config.filesFilter().isEmpty()) {
            compileFailures.forEach(failure -> this.compileWarnings.add(failure.path()));
        } else {
            compileFailures.stream()
                    .filter(failure -> config.filesFilter().contains(failure.path()))
                    .forEach(failure -> this.compileWarnings.add(failure.path()));
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

        LOGGER.info("Generating diagram with partition conf: " + partitionConf);

        PartitionPlacement placementStrategy = partitionConf.getRight();
        PartitionStrategy partitionStrategy = partitionConf.getLeft();

        List<Set<DiagramComponent>> cmpPartitions = partitionStrategy.apply();
        LOGGER.info(cmpPartitions.size() + " partitions were generated.");

        if (placementStrategy == PartitionPlacement.ONE_PER_DIAGRAM) {
            for (Set<DiagramComponent> currPartition : cmpPartitions) {
                this.insertDiagram(
                        new StriffDiagram(codeDiff, currPartition, diagramRels,
                                new DiagramDisplay(config.colorScheme(), this.cmpPkgs(currPartition)),
                                config));
            }
        } else if (placementStrategy == PartitionPlacement.CONDENSED) {
            Set<DiagramComponent> condensedPartition = cmpPartitions.stream().flatMap(Set::stream)
                    .collect(Collectors.toSet());

            this.insertDiagram(
                    new StriffDiagram(codeDiff, condensedPartition, diagramRels,
                            new DiagramDisplay(config.colorScheme(), this.cmpPkgs(condensedPartition)), config));
        } else {
            throw new IllegalArgumentException("Placement strategy " + placementStrategy + " is not supported!");
        }

        LOGGER.info(this.diagrams.size() + " diagrams were generated.");
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

    @JsonProperty("diagrams")
    public List<StriffDiagram> diagrams() {
        return this.diagrams;
    }

    @JsonProperty("compileWarnings")
    public Set<String> compileWarnings() {
        return this.compileWarnings;
    }
}
