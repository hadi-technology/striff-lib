package com.hadi.striff.spi;

import com.hadi.striff.diagram.DiagramComponent;
import com.hadi.striff.diagram.display.DiagramDisplay;

import java.util.List;

public interface ClassDecorator {

    ClassInsertionPoint insertionPoint();

    List<String> decorateClass(DiagramComponent component, DiagramDisplay display);

    default int order() {
        return 100;
    }
}
