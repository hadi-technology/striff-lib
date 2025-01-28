package com.hadii.striff.diagram.partition;

import com.hadii.striff.diagram.ComponentHelper;
import com.hadii.striff.diagram.DiagramComponent;
import com.hadii.striff.diagram.StriffDiagramModel;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.stream.Collectors.groupingBy;

/**
 * Partitions components based on what package they belong to.
 */
public final class PackagePartitionStrategy implements PartitionStrategy {

    private final List<Set<DiagramComponent>> partitions = new ArrayList<>();

    public PackagePartitionStrategy(StriffDiagramModel striffDiagramModel) {
        Map<String, List<DiagramComponent>> packagePartitions = striffDiagramModel.diagramCmps().stream()
                .collect(groupingBy(cmp -> ComponentHelper.packagePath(cmp.pkg())));
        // Convert map to list of hash sets.
        packagePartitions.values().forEach(
                diagramComponents -> partitions.add(new HashSet<>(diagramComponents)));
    }

    @Override
    public List<Set<DiagramComponent>> apply() {
        return this.partitions;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName();
    }

}
