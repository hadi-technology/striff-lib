package com.hadii.striff.diagram.display;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Map of source code packages to the color they should appear with.
 */
public class PkgColorsMap {

    String[] pkgColors = {
        "#f0fffa",
        "#f0fffd",
        "#f9fff0",
        "#f0f1ff",
        "#fff0ff",
        "#f0f8ff",
        "#f0ffff",
        "#fffaf0",
        "#f5f5dc",
        "#f8f4ff",
        "#f5f5dc",
        "#f8f8ff",
        "#f8f8f9",
        "#e7e8ee",
        "#fdecd5"
    };

    static final String DEFAULT_PKG_COLOR = LightDiagramColorScheme.PACKAGE_BG_COLOR;

    private final Map<String, String> pkgColorMap = new LinkedHashMap<>();

    /**
     * Deterministic ordering is important so PlantUML text output is stable
     * between runs; this avoids noisy diffs and regression test flakiness.
     */
    public PkgColorsMap(Set<String> pkgs) {
        List<String> sortedPkgs = new ArrayList<>(pkgs);
        Collections.sort(sortedPkgs);
        for (int i = 0; i < sortedPkgs.size(); i++) {
            String currColor = this.pkgColors[i % this.pkgColors.length];
            this.pkgColorMap.put(sortedPkgs.get(i), currColor);
        }
    }

    public String color(String pkg) {
        return this.pkgColorMap.getOrDefault(pkg, DEFAULT_PKG_COLOR);
    }

    public Set<Map.Entry<String, String>> mappings() {
        return this.pkgColorMap.entrySet();
    }
}
