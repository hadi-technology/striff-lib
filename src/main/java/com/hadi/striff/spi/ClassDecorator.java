package com.hadi.striff.spi;

import com.hadi.striff.diagram.DiagramComponent;
import com.hadi.striff.diagram.display.DiagramDisplay;

import java.util.List;

/**
 * Adds extra PlantUML lines inside rendered component blocks.
 * Implementations should be deterministic and side-effect free.
 */
public interface ClassDecorator {

    /**
     * Controls whether inserted PlantUML is placed before or after the main body.
     *
     * @return insertion point inside the class body
     */
    ClassInsertionPoint insertionPoint();

    /**
     * Each returned entry is treated as a line of PlantUML. Trailing newlines are
     * normalized during rendering, so decorators do not need to include them.
     *
     * @param component component being rendered
     * @param display resolved diagram display settings
     * @return extra PlantUML lines to inject
     */
    List<String> decorateClass(DiagramComponent component, DiagramDisplay display);

    /**
     * Lower values run first. Ties are broken deterministically by implementation
     * class name.
     *
     * @return ordering weight
     */
    default int order() {
        return 100;
    }
}
