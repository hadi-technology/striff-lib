package com.hadi.striff.spi;

import java.util.List;

public interface PackageDecorator {

    List<String> decoratePackage(PackageDecoratorContext context);

    default int order() {
        return 100;
    }
}
