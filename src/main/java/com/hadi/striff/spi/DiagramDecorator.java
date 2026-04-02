package com.hadi.striff.spi;

import com.hadi.striff.diagram.display.DiagramDisplay;

import java.util.List;

/**
 * Adds PlantUML lines at diagram scope before content or after relations.
 */
public interface DiagramDecorator {

    /**
     * @param display resolved diagram display settings
     * @return diagram-wide PlantUML lines to inject
     */
    List<String> decorateDiagram(DiagramDisplay display);

    /**
     * Controls where the returned lines are inserted in the final PlantUML text.
     *
     * @return diagram decoration placement
     */
    default DiagramDecoratorPlacement placement() {
        return DiagramDecoratorPlacement.AFTER_RELATIONS;
    }

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
