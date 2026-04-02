package com.hadi.striff.diagram.display;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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

    public DiagramDisplay(DiagramColorScheme diagramCS, Map<String, String> pkgColors) {
        this.pkgColorsMap = new PkgColorsMap(pkgColors);
        this.diagramCS = diagramCS;
    }

    public DiagramColorScheme colorScheme() {
        return this.diagramCS;
    }

    public List<java.util.Map.Entry<String, String>> pkgColorMappings() {
        return new ArrayList<>(this.pkgColorsMap.mappings());
    }

    public DiagramDisplay withPackageColors(Map<String, String> pkgColors) {
        if (pkgColors == null || pkgColors.isEmpty()) {
            return this;
        }
        Map<String, String> mergedPkgColors = new LinkedHashMap<>(this.pkgColorsMap.asMap());
        for (Map.Entry<String, String> entry : pkgColors.entrySet()) {
            if (entry.getKey() == null || entry.getValue() == null || entry.getValue().isBlank()) {
                continue;
            }
            mergedPkgColors.put(entry.getKey(), entry.getValue());
        }
        return new DiagramDisplay(this.diagramCS, mergedPkgColors);
    }

    public DiagramDisplay merge(DiagramDisplayOverride override) {
        if (override == null) {
            return this;
        }
        return new DiagramDisplay(override.applyTo(this.diagramCS), this.pkgColorsMap.asMap());
    }
}
