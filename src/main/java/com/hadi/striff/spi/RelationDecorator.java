package com.hadi.striff.spi;

import com.hadi.striff.diagram.display.DiagramDisplay;
import com.hadi.striff.extractor.ComponentRelation;

import java.util.List;

/**
 * Adds extra PlantUML lines adjacent to rendered relations.
 */
public interface RelationDecorator {

    /**
     * @param relation forward relation currently being rendered
     * @param reverseRelation reverse relation between the same endpoints, if any
     * @param display resolved diagram display settings
     * @return extra PlantUML lines to append after the rendered relation
     */
    List<String> decorateRelation(ComponentRelation relation, ComponentRelation reverseRelation, DiagramDisplay display);

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
