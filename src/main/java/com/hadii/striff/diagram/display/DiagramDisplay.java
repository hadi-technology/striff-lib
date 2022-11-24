package com.hadii.striff.diagram.display;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Map of source code packages to the color they should appear with.
 */
public class DiagramDisplay {

    private final PkgColorsMap pkgColorsMap;
    private final DiagramColorScheme diagramCS;

    public DiagramDisplay(DiagramColorScheme diagramCS, Set<String> allPkgs) {
        this.pkgColorsMap = new PkgColorsMap(allPkgs);
        this.diagramCS = diagramCS;
    }

    public DiagramColorScheme colorScheme() {
        return this.diagramCS;
    }

    public List<java.util.Map.Entry<String, String>> pkgColorMappings() {
        return new ArrayList<>(this.pkgColorsMap.mappings());
    }
}
