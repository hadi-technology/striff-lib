package com.hadii.striff.diagram.plantuml;

public final class PUMLDiagramText {

    private PUMLDiagramText() {
    }

    /**
     * Returns the PlantUML text for the provided diagram data without rendering.
     */
    public static String build(PUMLDiagramData data) {
        return new PUMLClassDiagramCode(data).code();
    }
}
