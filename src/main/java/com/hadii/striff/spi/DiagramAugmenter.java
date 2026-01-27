package com.hadii.striff.spi;

import com.hadii.striff.diagram.DiagramComponent;
import com.hadii.striff.parse.CodeDiff;

import java.util.Set;

public interface DiagramAugmenter {

    void augment(CodeDiff diff, Set<DiagramComponent> components);

    default int order() {
        return 100;
    }
}
