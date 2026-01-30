package com.hadi.striff.diagram.partition;

import com.hadi.striff.diagram.DiagramComponent;

import java.util.List;
import java.util.Set;

public interface PartitionStrategy {

    /**
     * Returns the final set of partitioned components.
     */
    List<Set<DiagramComponent>> apply();
}
