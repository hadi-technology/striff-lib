package com.hadi.striff.spi;

import com.hadi.striff.diagram.display.DiagramDisplay;
import com.hadi.striff.extractor.ComponentRelation;

import java.util.List;

public interface RelationDecorator {

    List<String> decorateRelation(ComponentRelation relation, ComponentRelation reverseRelation, DiagramDisplay display);

    default int order() {
        return 100;
    }
}
