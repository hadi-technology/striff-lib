package com.clarity.binary.metrics;

import com.clarity.sourcemodel.Component;
import com.clarity.sourcemodel.OOPSourceCodeModel;

import java.util.HashSet;
import java.util.Set;

/**
 * Represents the FanIn metric, which is a measure of the number of other classes that reference the given class.
 */
public class FanInMetric implements Metric {

    private Component component;
    private OOPSourceCodeModel srcModel;

    public FanInMetric(OOPSourceCodeModel model, Component component) {
        this.component = component;
        this.srcModel = model;
    }

    public double value() {
        Set<String> invokingClasses = new HashSet<>();
        this.srcModel.components().forEach((tmpComponent) -> {
            if (tmpComponent.componentType().isBaseComponent()) {
                tmpComponent.componentInvocations().forEach(invocation -> {
                    if (invocation.invokedComponent().equals(component.uniqueName())) {
                        invokingClasses.add(tmpComponent.uniqueName());

                    }
                });
            }
        });
        return (double) invokingClasses.size();
    }
}
