package com.hadii.stiff.metrics;

import com.hadii.clarpse.sourcemodel.Component;

/**
 * Represents the Polymorphism Factor metric, which is a measure of the number of overridden methods
 * compared to the number of methods.
 */
public class ResponseForClassMetric implements Metric {

    private Component component;

    public ResponseForClassMetric(Component component) {
        this.component = component;
    }

    public double value() {
        return this.component.cyclo();
    }
}
