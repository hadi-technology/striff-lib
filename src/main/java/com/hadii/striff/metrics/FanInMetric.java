package com.hadii.striff.metrics;

import com.hadii.clarpse.sourcemodel.Component;
import com.hadii.clarpse.sourcemodel.OOPSourceCodeModel;

import java.util.HashSet;
import java.util.Set;

/**
 * Represents the FanIn metric, which is a measure of the number of other classes that reference the given class.
 */
public class FanInMetric implements Metric {

    private final Component component;
    private final OOPSourceCodeModel srcModel;

    public FanInMetric(OOPSourceCodeModel model, Component component) {
        this.component = component;
        this.srcModel = model;
    }

    public double value() {
        Set<String> invokingClasses = new HashSet<>();
        this.srcModel.components().forEach((tmpComponent) -> {
            if (tmpComponent.componentType().isBaseComponent()) {
                tmpComponent.references().forEach(invocation -> {
                    if (invocation.invokedComponent().equals(component.uniqueName())) {
                        invokingClasses.add(tmpComponent.uniqueName());

                    }
                });
            }
        });
        return invokingClasses.size();
    }
}
