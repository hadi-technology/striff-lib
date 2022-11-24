package com.hadii.striff.diagram.partition;

import com.hadii.striff.diagram.DiagramComponent;
import com.hadii.striff.diagram.StriffDiagramModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Partitions all components into a single partition.
 */
public final class SinglePartitionStrategy implements PartitionStrategy {

    List<Set<DiagramComponent>> partitions = new ArrayList<>();

    public SinglePartitionStrategy(StriffDiagramModel striffDiagramModel) {
        this.partitions.add(striffDiagramModel.diagramCmps());
    }

    @Override
    public List<Set<DiagramComponent>> apply() {
        return this.partitions;
    }
}
