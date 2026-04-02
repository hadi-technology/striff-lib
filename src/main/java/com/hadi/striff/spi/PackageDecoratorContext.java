package com.hadi.striff.spi;

import com.hadi.striff.diagram.DiagramComponent;
import com.hadi.striff.diagram.display.DiagramDisplay;

import java.util.Set;

/**
 * Rendering context exposed to {@link PackageDecorator} implementations.
 *
 * @param packagePath package being rendered, or blank for the default package
 * @param packageComponents components contained directly in the package
 * @param diagramComponents all components in the current diagram
 * @param display resolved display settings for the current diagram
 */
public record PackageDecoratorContext(
        String packagePath,
        Set<DiagramComponent> packageComponents,
        Set<DiagramComponent> diagramComponents,
        DiagramDisplay display) {
}
