package com.hadi.striff.spi;

import com.hadi.striff.diagram.DiagramComponent;
import com.hadi.striff.parse.CodeDiff;

import java.util.Set;

/**
 * Augments diagram components before rendering.
 * Implementations can attach additional metadata to components via
 * {@link DiagramComponent#putAugmentation(String, String)}.
 */
public interface DiagramAugmenter {

    /**
     * Applies diagram-specific augmentations to the provided components.
     *
     * @param diff complete code diff driving the diagram
     * @param components components selected for display
     */
    void augment(CodeDiff diff, Set<DiagramComponent> components);

    /**
     * Ordering used when multiple augmenters are loaded through SPI.
     * Lower values run first.
     *
     * @return execution order
     */
    default int order() {
        return 100;
    }
}
