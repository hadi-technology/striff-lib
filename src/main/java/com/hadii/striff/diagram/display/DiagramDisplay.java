package com.hadii.striff.diagram.display;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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

    public DiagramDisplay merge(DiagramDisplayOverride override) {
        if (override == null) {
            return this;
        }
        DiagramColorScheme mergedScheme = new DiagramColorScheme() {
            @Override
            public String defaultFontName() {
                return pick(override.defaultFontName(), diagramCS.defaultFontName());
            }

            @Override
            public String backgroundColor() {
                return pick(override.backgroundColor(), diagramCS.backgroundColor());
            }

            @Override
            public String defaultClassHeaderColor() {
                return pick(override.defaultClassHeaderColor(), diagramCS.defaultClassHeaderColor());
            }

            @Override
            public String classArrowFontName() {
                return pick(override.classArrowFontName(), diagramCS.classArrowFontName());
            }

            @Override
            public String classArrowColor() {
                return pick(override.classArrowColor(), diagramCS.classArrowColor());
            }

            @Override
            public String objectColorBackground() {
                return pick(override.objectColorBackground(), diagramCS.objectColorBackground());
            }

            @Override
            public String classFontSize() {
                return pick(override.classFontSize(), diagramCS.classFontSize());
            }

            @Override
            public String classArrowFontColor() {
                return pick(override.classArrowFontColor(), diagramCS.classArrowFontColor());
            }

            @Override
            public String classArrowFontSize() {
                return pick(override.classArrowFontSize(), diagramCS.classArrowFontSize());
            }

            @Override
            public String legendBackgroundColor() {
                return pick(override.legendBackgroundColor(), diagramCS.legendBackgroundColor());
            }

            @Override
            public String modifiedComponentColor() {
                return pick(override.modifiedComponentColor(), diagramCS.modifiedComponentColor());
            }

            @Override
            public String minClassWidth() {
                return pick(override.minClassWidth(), diagramCS.minClassWidth());
            }

            @Override
            public String classFontColor() {
                return pick(override.classFontColor(), diagramCS.classFontColor());
            }

            @Override
            public String classFontName() {
                return pick(override.classFontName(), diagramCS.classFontName());
            }

            @Override
            public String zoomOutIconColor() {
                return pick(override.zoomOutIconColor(), diagramCS.zoomOutIconColor());
            }

            @Override
            public String classBorderThickness() {
                return pick(override.classBorderThickness(), diagramCS.classBorderThickness());
            }

            @Override
            public String classAttributeFontName() {
                return pick(override.classAttributeFontName(), diagramCS.classAttributeFontName());
            }

            @Override
            public String titleFontColor() {
                return pick(override.titleFontColor(), diagramCS.titleFontColor());
            }

            @Override
            public String packageBackgroundColor() {
                return pick(override.packageBackgroundColor(), diagramCS.packageBackgroundColor());
            }

            @Override
            public String titleFontName() {
                return pick(override.titleFontName(), diagramCS.titleFontName());
            }

            @Override
            public String classHeaderBackgroundColor() {
                return pick(override.classHeaderBackgroundColor(), diagramCS.classHeaderBackgroundColor());
            }

            @Override
            public String packageBorderColor() {
                return pick(override.packageBorderColor(), diagramCS.packageBorderColor());
            }

            @Override
            public String packageBorderThickness() {
                return pick(override.packageBorderThickness(), diagramCS.packageBorderThickness());
            }

            @Override
            public String dropShadows() {
                return pick(override.dropShadows(), diagramCS.dropShadows());
            }

            @Override
            public String packageFontColor() {
                return pick(override.packageFontColor(), diagramCS.packageFontColor());
            }

            @Override
            public String arrowThickness() {
                return pick(override.arrowThickness(), diagramCS.arrowThickness());
            }

            @Override
            public String packageFontName() {
                return pick(override.packageFontName(), diagramCS.packageFontName());
            }

            @Override
            public String packageFontStyle() {
                return pick(override.packageFontStyle(), diagramCS.packageFontStyle());
            }

            @Override
            public String classBorderColor() {
                return pick(override.classBorderColor(), diagramCS.classBorderColor());
            }

            @Override
            public String addedComponentColor() {
                return pick(override.addedComponentColor(), diagramCS.addedComponentColor());
            }

            @Override
            public String addedRelationColor() {
                return pick(override.addedRelationColor(), diagramCS.addedRelationColor());
            }

            @Override
            public String deletedRelationColor() {
                return pick(override.deletedRelationColor(), diagramCS.deletedRelationColor());
            }

            @Override
            public String deletedComponentColor() {
                return pick(override.deletedComponentColor(), diagramCS.deletedComponentColor());
            }
        };
        Set<String> pkgs = pkgColorMappings().stream()
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
        return new DiagramDisplay(mergedScheme, pkgs);
    }

    private static String pick(String overrideValue, String baseValue) {
        return overrideValue != null ? overrideValue : baseValue;
    }
}
