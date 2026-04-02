package com.hadi.striff.spi;

import java.util.List;

/**
 * Adds extra PlantUML lines inside rendered package blocks.
 */
public interface PackageDecorator {

    /**
     * @param context package-level rendering context
     * @return extra PlantUML lines to inject into the package body
     */
    List<String> decoratePackage(PackageDecoratorContext context);

    /**
     * Lower values run first. Ties are broken deterministically by implementation
     * class name.
     *
     * @return ordering weight
     */
    default int order() {
        return 100;
    }
}
