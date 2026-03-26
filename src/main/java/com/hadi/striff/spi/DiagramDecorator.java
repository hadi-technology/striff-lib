package com.hadi.striff.spi;

import com.hadi.striff.diagram.display.DiagramDisplay;

import java.util.List;

public interface DiagramDecorator {

    List<String> decorateDiagram(DiagramDisplay display);

    default DiagramDecoratorPlacement placement() {
        return DiagramDecoratorPlacement.AFTER_RELATIONS;
    }

    default int order() {
        return 100;
    }
}
