package com.hadii.striff.diagram.display;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
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

    private static final Random RANDOM = new Random();

    private final Map<String, String> pkgColorMap = new HashMap<>();

    public PkgColorsMap(Set<String> pkgs) {
        pkgs.forEach(pkg -> {
            String currColor = this.pkgColors[RANDOM.nextInt(this.pkgColors.length)];
            this.pkgColorMap.put(pkg, currColor);
        });
    }

    public String color(String pkg) {
        return this.pkgColorMap.getOrDefault(pkg, DEFAULT_PKG_COLOR);
    }

    public Set<Map.Entry<String, String>> mappings() {
        return this.pkgColorMap.entrySet();
    }
}
