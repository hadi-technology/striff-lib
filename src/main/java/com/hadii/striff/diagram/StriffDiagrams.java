package com.hadii.striff.diagram;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;

import com.hadii.clarpse.compiler.ProjectFile;
import com.hadii.striff.NoStructuralChangesException;
import com.hadii.striff.StriffConfig;
import com.hadii.striff.diagram.display.DiagramDisplay;
import com.hadii.striff.diagram.display.LightDiagramColorScheme;
import com.hadii.striff.diagram.display.OutputMode;
import com.hadii.striff.diagram.display.PartitionPlacement;
import com.hadii.striff.diagram.partition.PackagePartitionStrategy;
import com.hadii.striff.diagram.partition.PartitionStrategy;
import com.hadii.striff.diagram.plantuml.PUMLDrawException;
import com.hadii.striff.parse.CodeDiff;

public class StriffDiagrams {

    private final List<StriffDiagram> diagrams;
    private final Set<String> compileWarnings = new HashSet<>();

    public StriffDiagrams(CodeDiff codeDiff, StriffConfig config)
        throws NoStructuralChangesException, PUMLDrawException, IOException {
        this(codeDiff, config, Collections.emptySet());
    }

    public StriffDiagrams(CodeDiff codeDiff, StriffConfig config, Set<ProjectFile> compileFailures)
        throws PUMLDrawException, NoStructuralChangesException, IOException {
        this.diagrams = diagrams(codeDiff, partitionConfig(
            genStriffModel(codeDiff, config.filesFilter), config));
        // Keep only those warnings which are relevant to the generated striff diagrams.
        Set<String> diagramFiles = diagrams.stream().flatMap(
            diagram -> diagram.cmps().stream()).map(DiagramComponent::sourceFile).collect(Collectors.toSet());
        compileFailures.stream().filter(file -> diagramFiles.contains(file.path())).forEach(failure -> {
            this.compileWarnings.add(failure.path());
        });
    }

    private StriffDiagramModel genStriffModel(CodeDiff diffedModel, Set<String> sourceFilesFilter)
        throws NoStructuralChangesException {
        StriffDiagramModel striffDiagramModel;
        striffDiagramModel = new StriffDiagramModel(diffedModel, sourceFilesFilter);
        if (striffDiagramModel.diagramCmps().size() < 1) {
            throw new NoStructuralChangesException(
                "No structural changes were found between the given code bases!");
        }
        return striffDiagramModel;
    }

    private Pair<PartitionStrategy, PartitionPlacement> partitionConfig(
        StriffDiagramModel striffDiagramModel, StriffConfig config) {
        PartitionStrategy strategy;
        PartitionPlacement placementStrat;
        if (config.outputMode == OutputMode.DEFAULT) {
            strategy = new PackagePartitionStrategy(striffDiagramModel);
            placementStrat = PartitionPlacement.CONDENSED;
        } else {
            throw new IllegalArgumentException(
                "Output mode " + config.outputMode.name() + " is not currently supported!");
        }
        return Pair.of(strategy, placementStrat);
    }

    private List<StriffDiagram> diagrams(
        CodeDiff mergedModel, Pair<PartitionStrategy, PartitionPlacement> partitionConf
    ) throws IOException, PUMLDrawException {
        List<StriffDiagram> striffDiagrams = new ArrayList<>();
        PartitionPlacement placementStrat = partitionConf.getRight();
        PartitionStrategy partitionStrategy = partitionConf.getLeft();
        List<Set<DiagramComponent>> cmpPartitions = partitionStrategy.apply();
        if (placementStrat == PartitionPlacement.ONE_PER_DIAGRAM) {
            for (Set<DiagramComponent> currPartition : cmpPartitions) {
                striffDiagrams.add(new StriffDiagram(
                    mergedModel,
                    currPartition,
                    new DiagramDisplay(
                        new LightDiagramColorScheme(), this.cmpPkgs(currPartition)
                    )
                ));
            }
        } else if (placementStrat == PartitionPlacement.CONDENSED) {
            // Condense partitions into one diagram.
            Set<DiagramComponent> condensedPartition = cmpPartitions.stream().flatMap(Set::stream)
                                                                    .collect(Collectors.toSet());
            striffDiagrams.add(new StriffDiagram(
                mergedModel,
                condensedPartition,
                new DiagramDisplay(
                    new LightDiagramColorScheme(), this.cmpPkgs(condensedPartition)
                )
            ));
        } else {
            throw new IllegalArgumentException("Placement strategy " + placementStrat + " is "
                                                   + "not supported!");
        }
        return striffDiagrams;
    }

    private Set<String> cmpPkgs(Set<DiagramComponent> cmps) {
        return cmps.stream().map(DiagramComponent::packagePath).collect(Collectors.toSet());
    }

    public List<StriffDiagram> diagrams() {
        return this.diagrams;
    }

    public Set<String> compileWarnings() {
        return this.compileWarnings;
    }
}
