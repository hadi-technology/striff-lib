package com.hadii.striff.metrics;

import com.hadii.clarpse.sourcemodel.Component;

/**
 * Represents a Cyclomatic Complexity metric, which is a measure of the complexity of a given code unit.
 */
public class CycloMetric implements Metric {

    private final Component component;

    public CycloMetric(Component component) {
        this.component = component;
    }

    public double value() {
        return this.component.cyclo();
    }
}
