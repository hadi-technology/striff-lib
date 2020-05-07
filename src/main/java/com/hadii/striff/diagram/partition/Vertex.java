package com.hadii.striff.diagram.partition;

final class Vertex {

  private final String name;

  public Vertex(String name) {
    this.name = name;
  }
  
  @Override
  public String toString() {
    return name;
  }

  public String name() {
    return this.name;
  }

  @Override
  public int hashCode() {
    return this.name().hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if(!(obj instanceof Vertex)) return false;
    Vertex other = (Vertex) obj;
    return other.name().equals(this.name());
  }
}
