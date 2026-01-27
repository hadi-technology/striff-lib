package com.hadii.striff.spi;

import com.hadii.striff.diagram.DiagramComponent;
import com.hadii.striff.diagram.display.DiagramDisplay;

import java.util.List;

public interface ClassDecorator {

    ClassInsertionPoint insertionPoint();

    List<String> decorateClass(DiagramComponent component, DiagramDisplay display);

    default int order() {
        return 100;
    }
}
