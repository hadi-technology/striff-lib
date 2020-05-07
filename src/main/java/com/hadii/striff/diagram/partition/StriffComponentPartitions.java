package com.hadii.striff.diagram.partition;

import com.hadii.striff.diagram.DiagramComponent;
import com.hadii.striff.extractor.ComponentRelations;
import org.gephi.graph.api.Column;
import org.gephi.graph.api.DirectedGraph;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.impl.GraphModelImpl;
import org.gephi.statistics.plugin.Modularity;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Represents partitions of a larger set of components to facilitate the creation of smaller Striff diagrams.
 */
public final class StriffComponentPartitions {

    private final Map<String, DiagramComponent> componentNameMap = new HashMap<>();
    private final Map<Integer, Set<DiagramComponent>> partitions = new HashMap<>();

    public StriffComponentPartitions(Set<DiagramComponent> unpartitionedComponents,
                                     ComponentRelations componentRelations) {

        unpartitionedComponents.forEach(diagramComponent -> {
            if (!diagramComponent.componentType().isBaseComponent()) {
                throw new IllegalArgumentException("Components that are being partitioned must be base components!");
            }
            this.componentNameMap.put(diagramComponent.uniqueName(), diagramComponent);
        });
        generatePartitions(unpartitionedComponents, componentRelations);
    }

    private void generatePartitions(Set<DiagramComponent> unpartitionedComponents, ComponentRelations relations) {
        Modularity modularity = new Modularity();
        GraphModel graphModel = genGraphModel(unpartitionedComponents, relations);
        modularity.setUseWeight(false);
        modularity.setResolution(1.75);
        modularity.setRandom(false);
        modularity.execute(graphModel);
        Column column = graphModel.getNodeTable().getColumn(Modularity.MODULARITY_CLASS);
        graphModel.getGraph().getNodes().forEach(node -> partitionNode((String) node.getId(), (int) node.getAttribute(column)));
    }

    private void partitionNode(String uniqueName, int index) {
        DiagramComponent diagramComponent = componentNameMap.get(uniqueName);
        if (!this.partitions.containsKey(index)) {
            this.partitions.put(index, new HashSet<>());
        }
        this.partitions.get(index).add(diagramComponent);
    }

    private GraphModel genGraphModel(Set<DiagramComponent> startingPartition, ComponentRelations relations) {
        GraphModel graphModel = new GraphModelImpl();
        // populate Graph object
        DirectedGraph directedGraph = graphModel.getDirectedGraph();
        startingPartition.forEach(diagramComponent -> directedGraph.addNode(graphModel.factory().newNode(diagramComponent.uniqueName())));
        startingPartition.forEach(diagramComponent -> {
            if (relations.hasRelationsforComponent(diagramComponent)) {
                relations.componentRelations(diagramComponent).forEach(componentRelation -> {
                    if (directedGraph.hasNode(componentRelation.targetComponent().uniqueName())) {
                        directedGraph.addEdge(graphModel.factory().newEdge(
                                String.valueOf(componentRelation.hashCode()),
                                directedGraph.getNode(componentRelation.originalComponent().uniqueName()),
                                directedGraph.getNode(componentRelation.targetComponent().uniqueName()),
                                0,
                                componentRelation.associationType().strength(),
                                true
                        ));
                    }
                });
            }
        });
        return graphModel;
    }

    public Map<Integer, Set<DiagramComponent>> partitions () {
        return this.partitions;
    }
}
