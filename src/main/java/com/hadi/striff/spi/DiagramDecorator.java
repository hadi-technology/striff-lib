package com.hadi.striff.spi;

import com.hadi.striff.diagram.display.DiagramDisplay;

import java.util.List;

public interface DiagramDecorator {

    List<String> decorateDiagram(DiagramDisplay display);

    default int order() {
        return 100;
    }
}
