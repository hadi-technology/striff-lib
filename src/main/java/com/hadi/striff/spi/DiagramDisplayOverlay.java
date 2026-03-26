package com.hadi.striff.spi;

import com.hadi.striff.diagram.DiagramComponent;
import com.hadi.striff.diagram.display.DiagramDisplay;

import java.util.Set;

public interface DiagramDisplayOverlay {

    DiagramDisplay overlay(DiagramDisplay display, Set<DiagramComponent> components);

    default int order() {
        return 100;
    }
}
