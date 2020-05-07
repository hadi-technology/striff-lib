package com.hadii.striff.metrics;

import com.hadii.clarpse.reference.ComponentReference;
import com.hadii.clarpse.sourcemodel.Component;

import java.util.HashSet;
import java.util.Set;

/**
 * Represents the Fanout metric, which is a measure of the number of other classes referenced by a class.
 */
public class FanOutMetric implements Metric {

    private final Component component;

    public FanOutMetric(Component component) {
        this.component = component;
    }

    public double value() {
        Set<String > referencedComponents = new HashSet<>();
        for (ComponentReference invocation : this.component.references()) {
            referencedComponents.add(invocation.invokedComponent());
        }
        return referencedComponents.size();
    }
}
