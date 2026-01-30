package com.hadi.striff.spi;

import com.hadi.striff.diagram.DiagramComponent;
import com.hadi.striff.parse.CodeDiff;

import java.util.Set;

public interface DiagramAugmenter {

    void augment(CodeDiff diff, Set<DiagramComponent> components);

    default int order() {
        return 100;
    }
}
