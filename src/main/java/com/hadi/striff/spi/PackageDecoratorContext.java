package com.hadi.striff.spi;

import com.hadi.striff.diagram.DiagramComponent;
import com.hadi.striff.diagram.display.DiagramDisplay;

import java.util.Set;

public final class PackageDecoratorContext {
    private final String packagePath;
    private final Set<DiagramComponent> packageComponents;
    private final Set<DiagramComponent> diagramComponents;
    private final DiagramDisplay display;

    public PackageDecoratorContext(
            String packagePath,
            Set<DiagramComponent> packageComponents,
            Set<DiagramComponent> diagramComponents,
            DiagramDisplay display) {
        this.packagePath = packagePath;
        this.packageComponents = packageComponents;
        this.diagramComponents = diagramComponents;
        this.display = display;
    }

    public String packagePath() {
        return packagePath;
    }

    public Set<DiagramComponent> packageComponents() {
        return packageComponents;
    }

    public Set<DiagramComponent> diagramComponents() {
        return diagramComponents;
    }

    public DiagramDisplay display() {
        return display;
    }
}
