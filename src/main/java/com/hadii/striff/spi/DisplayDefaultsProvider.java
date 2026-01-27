package com.hadii.striff.spi;

import com.hadii.striff.diagram.display.DiagramDisplay;
import com.hadii.striff.diagram.display.DiagramDisplayOverride;

public interface DisplayDefaultsProvider {

    DiagramDisplayOverride defaultsFor(DiagramDisplay baseDisplay);

    default int order() {
        return 100;
    }
}
