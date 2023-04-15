package com.hadii.striff.diagram.partition;

import com.hadii.striff.diagram.DiagramComponent;

import java.util.List;
import java.util.Set;

public interface PartitionStrategy {

    /**
     * Returns the final set of partitioned components.
     */
    List<Set<DiagramComponent>> apply();
}
