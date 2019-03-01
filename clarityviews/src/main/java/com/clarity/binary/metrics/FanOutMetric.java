package com.clarity.binary.metrics;

import com.clarity.invocation.ComponentInvocation;
import com.clarity.sourcemodel.Component;

import java.util.HashSet;
import java.util.Set;

/**
 * Represents the Fanout metric, which is a measure of the number of other classes referenced by a class.
 */
public class FanOutMetric implements Metric {

    private Component component;

    public FanOutMetric(Component component) {
        this.component = component;
    }

    public double value() {
        Set<String > invokedClasses = new HashSet<>();
        for (ComponentInvocation invocation : this.component.componentInvocations()) {
            invokedClasses.add(invocation.invokedComponent());
        }
        return (double) invokedClasses.size();
    }
}
