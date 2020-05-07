package com.hadii.striff.diagram.partition;

import com.hadii.striff.diagram.DiagramComponent;
import com.hadii.striff.extractor.ComponentRelations;

import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Partitions a set of components into two groups such that the weights of the relations between the groups
 * (cutting cost) is minimised.
 */
final class KernighanLinPartitions {

    private final Map<String, DiagramComponent> components = new HashMap<>();
    private final int partitionSize;
    private final Graph graph;
    private final VertexGroup vGroupA;
    private final VertexGroup vGroupB;

    public KernighanLinPartitions(Set<DiagramComponent> components, ComponentRelations relations) {
        this.graph = new Graph(components, relations);
        this.partitionSize = this.graph.getVertices().size() / 2;
        this.vGroupA = new VertexGroup();
        this.vGroupB = new VertexGroup();
        for (DiagramComponent component : components) {
            this.components.put(component.uniqueName(), component);
        }
        if (this.graph.getVertices().size() != partitionSize * 2)
            throw new IllegalArgumentException("Vertices must be even in number!");
        partitionGraph();
    }

    private void partitionGraph() {
        // Split vertices into A and B
        int i = 0;
        for (Vertex v : this.graph.getVertices()) {
            (++i > partitionSize ? vGroupB : vGroupA).add(v);
        }
        VertexGroup unswappedA = new VertexGroup(vGroupA);
        VertexGroup unswappedB = new VertexGroup(vGroupB);
        System.out.println(vGroupA.size() + " " + vGroupB.size());
        doAllSwaps(unswappedA, unswappedB);
    }

    /**
     * Performs |V|/2 swaps and chooses the one with least cut cost one
     **/
    private void doAllSwaps(VertexGroup unswappedA, VertexGroup unswappedB) {
        LinkedList<Pair<Vertex>> swaps = new LinkedList<>();
        double minCost = Double.POSITIVE_INFINITY;
        int minId = -1;

        for (int i = 0; i < partitionSize; i++) {
            double cost = doSingleSwap(swaps, unswappedA, unswappedB);
            if (cost < minCost) {
                minCost = cost;
                minId = i;
            }
        }
        // Unwind swaps
        while (swaps.size() - 1 > minId) {
            Pair<Vertex> pair = swaps.pop();
            // unswap
            swapVertices(vGroupA, pair.second, vGroupB, pair.first);
        }
    }

    /**
     * Chooses the least cost swap and performs it
     **/
    private double doSingleSwap(Deque<Pair<Vertex>> swaps, VertexGroup unswappedA, VertexGroup unswappedB) {
        Pair<Vertex> maxPair = null;
        double maxGain = Double.NEGATIVE_INFINITY;
        for (Vertex v_a : unswappedA) {
            for (Vertex v_b : unswappedB) {
                Edge e = graph.findEdge(v_a, v_b);
                double edge_cost = (e != null) ? e.weight() : 0;
                // Calculate the gain in cost if these vertices were swapped
                // subtract 2*edge_cost because this edge will still be an external edge
                // after swapping
                double gain = getVertexCost(v_a) + getVertexCost(v_b) - 2 * edge_cost;
                if (gain > maxGain) {
                    maxPair = new Pair<>(v_a, v_b);
                    maxGain = gain;
                }
            }
        }
        assert maxPair != null;
        swapVertices(vGroupA, maxPair.first, vGroupB, maxPair.second);
        swaps.push(maxPair);
        unswappedA.remove(maxPair.first);
        unswappedB.remove(maxPair.second);
        return getCutCost();
    }

    /**
     * Returns the difference of external cost and internal cost of this vertex.
     * When moving a vertex from within group A, all internal edges become external
     * edges and vice versa.
     **/
    private double getVertexCost(Vertex v) {
        double cost = 0;
        boolean v1isInA = vGroupA.contains(v);
        for (Vertex v2 : Objects.requireNonNull(graph.getNeighbors(v))) {
            boolean v2isInA = vGroupA.contains(v2);
            Edge edge = graph.findEdge(v, v2);
            if (v1isInA == v2isInA) {
                assert edge != null;
                cost -= edge.weight();
            } else { // external
                assert edge != null;
                cost += edge.weight();
            }
        }
        return cost;
    }

    /**
     * Returns the sum of the costs of all edges between A and B
     **/
    public double getCutCost() {
        double cost = 0;
        for (Edge edge : graph.getEdges()) {
            Pair<Vertex> endpoints = graph.getEndpoints(edge);
            boolean firstInA = vGroupA.contains(endpoints.first);
            boolean secondInA = vGroupA.contains(endpoints.second);
            if (firstInA != secondInA) // external
                cost += edge.weight();
        }
        return cost;
    }

    /**
     * Swaps va and vb in groups a and b
     **/
    private void swapVertices(VertexGroup a, Vertex va, VertexGroup b, Vertex vb) {
        if (!a.contains(va) || a.contains(vb) ||
                !b.contains(vb) || b.contains(va)) throw new RuntimeException("Invalid swap");
        a.remove(va);
        a.add(vb);
        b.remove(vb);
        b.add(va);
    }

    public Set<DiagramComponent> groupA() {
        Set<DiagramComponent> groupA = new HashSet<>();
        this.vGroupA.forEach(vertex -> groupA.add(this.components.get(vertex.name())));
        return groupA;
    }

    public Set<DiagramComponent> groupB() {
        Set<DiagramComponent> groupB = new HashSet<>();
        this.vGroupB.forEach(vertex -> groupB.add(this.components.get(vertex.name())));
        return groupB;
    }
}
