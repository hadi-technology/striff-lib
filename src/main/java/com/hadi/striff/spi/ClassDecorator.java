package com.hadi.striff.spi;

import com.hadi.striff.diagram.DiagramComponent;
import com.hadi.striff.diagram.display.DiagramDisplay;

import java.util.List;

public interface ClassDecorator {

    ClassInsertionPoint insertionPoint();

    /**
     * Each returned entry is treated as a line of PlantUML. Trailing newlines are
     * normalized during rendering, so decorators do not need to include them.
     */
    List<String> decorateClass(DiagramComponent component, DiagramDisplay display);

    default int order() {
        return 100;
    }
}
