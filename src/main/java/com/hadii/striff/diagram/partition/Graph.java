package com.hadii.striff.diagram.partition;

import com.hadii.striff.diagram.DiagramComponent;
import com.hadii.striff.extractor.ComponentRelations;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

final class Graph {

    final private Map<Vertex, Map<Vertex, Edge>> vertices;
    final private Map<Edge, Pair<Vertex>> edges;

    public Graph() {
        // Maps vertices to a map of vertices to incident edges
        vertices = new HashMap<Vertex, Map<Vertex, Edge>>();
        // Maps edges to edge endpoints
        edges = new HashMap<Edge, Pair<Vertex>>();
    }

    public Graph(Set<DiagramComponent> components, ComponentRelations relations) {
        this();
        // populate Graph object
        components.forEach(diagramComponent -> {
            addVertex(new Vertex(diagramComponent.uniqueName()));
        });
        components.forEach(diagramComponent -> {
            if (relations.hasRelationsforComponent(diagramComponent)) {
                relations.componentRelations(diagramComponent).forEach(componentRelation -> {
                    if (containsVertex(new Vertex(componentRelation.targetComponent().uniqueName()))) {
                        addEdge(new Edge(componentRelation.associationType()),
                                new Vertex(componentRelation.originalComponent().uniqueName()),
                                new Vertex(componentRelation.targetComponent().uniqueName()));
                    }
                });
            }
        });
    }

    public boolean addVertex(Vertex v) {
        if (containsVertex(v)) return false;
        vertices.put(v, new HashMap<Vertex, Edge>());
        return true;
    }

    public boolean addEdge(Edge edge, Vertex v1, Vertex v2) {
        if (!containsVertex(v1) || !containsVertex(v2)) return false;
        if (findEdge(v1, v2) != null) return false;

        Pair<Vertex> pair = new Pair<Vertex>(v1, v2);
        edges.put(edge, pair);
        vertices.get(v1).put(v2, edge);
        vertices.get(v2).put(v1, edge);

        return true;
    }

    public boolean containsVertex(Vertex v) {
        return vertices.containsKey(v);
    }

    /**
     * Finds an edge if any between v1 and v2
     **/
    public Edge findEdge(Vertex v1, Vertex v2) {
        if (!containsVertex(v1) || !containsVertex(v2))
            return null;
        return vertices.get(v1).get(v2);
    }

    /**
     * Gets the vertices directly connected to v
     **/
    public Collection<Vertex> getNeighbors(Vertex v) {
        if (!containsVertex(v)) return null;
        return vertices.get(v).keySet();
    }

    public Set<Edge> getEdges() {
        return edges.keySet();
    }

    public Set<Vertex> getVertices() {
        return vertices.keySet();
    }

    /**
     * Returns a pair of vertices that connects by edge e
     **/
    public Pair<Vertex> getEndpoints(Edge e) {
        return edges.get(e);
    }
}
