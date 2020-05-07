package com.hadii.striff.diagram.partition;

import com.hadii.striff.diagram.DiagramConstants;

final class Edge {

  private final double weight;

  public Edge(DiagramConstants.ComponentAssociation componentAssociation) {
    this.weight = componentAssociation.strength();
  }

  public double weight() {
    return this.weight;
  }
}
