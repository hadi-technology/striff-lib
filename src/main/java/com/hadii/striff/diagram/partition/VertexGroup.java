package com.hadii.striff.diagram.partition;

import java.util.HashSet;

final class VertexGroup extends HashSet<Vertex> {

    public VertexGroup(HashSet<Vertex> clone) {
        super(clone);
    }

    public VertexGroup() { }
}