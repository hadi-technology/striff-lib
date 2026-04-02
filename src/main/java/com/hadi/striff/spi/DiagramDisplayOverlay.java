package com.hadi.striff.spi;

import com.hadi.striff.diagram.DiagramComponent;
import com.hadi.striff.diagram.display.DiagramDisplay;

import java.util.Set;

/**
 * Allows extensions to adjust resolved display settings after the base display
 * and configured overrides have been applied.
 */
public interface DiagramDisplayOverlay {

    /**
     * @param display current resolved display settings
     * @param components components that will appear in the diagram
     * @return new display settings, or {@code null} to leave the display unchanged
     */
    DiagramDisplay overlay(DiagramDisplay display, Set<DiagramComponent> components);

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
