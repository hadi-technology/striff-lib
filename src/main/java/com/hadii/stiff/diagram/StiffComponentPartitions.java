package com.hadii.stiff.diagram;

import com.hadii.clarpse.sourcemodel.OOPSourceModelConstants;
import com.hadii.stiff.StiffCodeModel;
import com.hadii.stiff.extractor.ComponentRelation;
import com.hadii.stiff.extractor.ComponentRelations;
import edu.emory.mathcs.backport.java.util.Collections;
import org.gephi.graph.api.Column;
import org.gephi.graph.api.DirectedGraph;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.impl.GraphModelImpl;
import org.gephi.statistics.plugin.Modularity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Represents partitions of a larger set of components to facilitate the creation of smaller Striff diagrams.
 */
public final class StiffComponentPartitions {

    private final Map<String, DiagramComponent> componentNameMap = new HashMap<>();
    private final int softMaxSizeLimit;
    private final List<Set<DiagramComponent>> partitions = new ArrayList<>();
    private final Set<DiagramComponent> keyComponents;
    private final Set<ComponentRelation> keyRelations;
    private final ComponentRelations allRelations;
    private final int contextLevel;

    public StiffComponentPartitions(StiffCodeModel stiffModel, int softMaxSizeLimit, int contextLevel) {
        stiffModel.allComponents().forEach(diagramComponent -> {
            if (!diagramComponent.componentType().isBaseComponent()) {
                throw new IllegalArgumentException("Components that are being partitioned must be base components!");
            }
            this.componentNameMap.put(diagramComponent.uniqueName(), diagramComponent);
        });
        this.softMaxSizeLimit = softMaxSizeLimit;
        this.keyComponents = stiffModel.coreComponents();
        this.contextLevel = contextLevel;
        this.keyRelations = stiffModel.coreRelations();
        this.allRelations = new ComponentRelations(stiffModel.allRelations());
        generatePartitions(stiffModel.allComponents());
        // Final run to condense partitions in the overall partitions list
        mergeSmallerPartitions(this.partitions);
    }

    private void generatePartitions(Set<DiagramComponent> unpartitionedComponents) {
        Modularity modularity = new Modularity();
        GraphModel graphModel = genGraphModel(unpartitionedComponents);
        modularity.setUseWeight(true);
        modularity.setResolution(1.);
        modularity.setRandom(false);
        // Runs partitioning algorithm and adds new partition label to every node in the graph
        modularity.execute(graphModel);
        Column column = graphModel.getNodeTable().getColumn(Modularity.MODULARITY_CLASS);
        // Map graph partition labels to components..
        Map<Integer, Set<DiagramComponent>> graphPartitionsMap = new HashMap<>();
        graphModel.getGraph().getNodes().forEach(node -> {
            int index = (int) node.getAttribute(column);
            if (!graphPartitionsMap.containsKey(index)) {
                graphPartitionsMap.put(index, new HashSet<>());
            }
            graphPartitionsMap.get(index).add(componentNameMap.get(node.getId()));
        });
        List<Set<DiagramComponent>> generatedPartitions = new ArrayList<>(graphPartitionsMap.values());
        // Filter partitions based on the requirement of having at least one key component in each partition
        filterUnImportantPartitions(generatedPartitions);
        // Condense smaller partitions together
        mergeSmallerPartitions(generatedPartitions);
        for (Set<DiagramComponent> currPartition : generatedPartitions) {
            // For partitions that are too large and can be partitioned further, recursively partition them.
            if (currPartition.size() > this.softMaxSizeLimit * 1.5 && graphPartitionsMap.size() > 1) {
                generatePartitions(currPartition);
                // Otherwise, the partition looks good and can be added to our master list of partitions
            } else {
                this.partitions.add(currPartition);
            }
        }
    }

    private void mergeSmallerPartitions(List<Set<DiagramComponent>> partitions) {
        // For partitions that are too small, group them together with that partition they share the most relations with.
        for (int i = 0; i < partitions.size(); i++) {
            Set<DiagramComponent> currPartition = partitions.get(i);
            for (int y = 0; y < partitions.size(); y++) {
                if (y != i) {
                    Set<DiagramComponent> tmpPartition = partitions.get(y);
                    if (tmpPartition.size() + currPartition.size() < this.softMaxSizeLimit * 1.5) {
                        tmpPartition.addAll(currPartition);
                        partitions.remove(i);
                        i = -1;
                        break;
                    }
                }
            }
        }
    }

    private void filterUnImportantPartitions(List<Set<DiagramComponent>> partitions) {
        // Make sure partitions have at least one or more key components, and that the components in each partition are relevant
        // to the key components present in that partition.
        // Step 1: If a partition has no core components, it's not important, delete it.
        partitions.removeIf(partition -> Collections.disjoint(partition, this.keyComponents));
        // Step 2: Partitions should only consist of components that are related to the core components within that partition.
        for (Set<DiagramComponent> partition : partitions) {
            Set<DiagramComponent> partitionKeyComponents = partition.stream()
                    .distinct()
                    .filter(this.keyComponents::contains)
                    .collect(Collectors.toSet());
            // Key partition components consist of those components which are no more than contextLevel hops away from a key component.
            for (int i = 0; i < this.contextLevel; i++) {
                Set<DiagramComponent> currPartitionKeyComponents = new HashSet<>(partitionKeyComponents);
                for (DiagramComponent cmp : currPartitionKeyComponents) {
                    if (this.allRelations.hasRelationsforComponent(cmp)) {
                        for (ComponentRelation componentRelation : this.allRelations.componentRelations(cmp)) {
                            partitionKeyComponents.add(componentRelation.targetComponent());
                        }
                    }
                }
            }
            partition.retainAll(partitionKeyComponents);
        }
    }

    private GraphModel genGraphModel(Set<DiagramComponent> startingPartition) {
        GraphModel graphModel = new GraphModelImpl();
        // populate Graph object
        DirectedGraph directedGraph = graphModel.getDirectedGraph();
        startingPartition.forEach(diagramComponent -> directedGraph.addNode(graphModel.factory().newNode(diagramComponent.uniqueName())));
        startingPartition.forEach(diagramComponent -> {
            if (this.allRelations.hasRelationsforComponent(diagramComponent)) {
                this.allRelations.componentRelations(diagramComponent).forEach(componentRelation -> {
                    if (directedGraph.hasNode(componentRelation.targetComponent().uniqueName())) {
                        directedGraph.addEdge(graphModel.factory().newEdge(
                                String.valueOf(componentRelation.hashCode()),
                                directedGraph.getNode(componentRelation.originalComponent().uniqueName()),
                                directedGraph.getNode(componentRelation.targetComponent().uniqueName()),
                                0,
                                componentRelation.associationType().strength() * edgeWeightFactor(componentRelation),
                                true
                        ));
                    }
                });
            }
        });
        return graphModel;
    }

    public List<Set<DiagramComponent>> partitions() {
        return this.partitions;
    }

    /**
     * Boosts the default edge weight associated with the given relation based on certain factors.
     */
    private int edgeWeightFactor(ComponentRelation componentRelation) {
        int edgeWeightMultiplicationFactor = 1;
        if (this.keyComponents.contains(componentRelation.originalComponent())
                || componentRelation.originalComponent().packageName().equals(componentRelation.targetComponent().packageName())
                || componentRelation.originalComponent().componentType() == OOPSourceModelConstants.ComponentType.INTERFACE
                || componentRelation.originalComponent().modifiers().contains("abstract")) {
            edgeWeightMultiplicationFactor += 1;
        }
        if (this.keyComponents.contains(componentRelation.targetComponent())
                || this.keyRelations.contains(componentRelation)) {
            edgeWeightMultiplicationFactor += 2;
        }
        return edgeWeightMultiplicationFactor;
    }
}
