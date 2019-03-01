package com.clarity.binary.metrics;

import com.clarity.invocation.ComponentInvocation;
import com.clarity.sourcemodel.Component;
import com.clarity.sourcemodel.OOPSourceCodeModel;
import com.clarity.sourcemodel.OOPSourceModelConstants;

import java.util.Optional;

/**
 * Represents a Depth of Inheritance metric, which is a measure of how far down a class is declared in
 * the inheritance hierarchy.
 */
public class DITMetric implements Metric {

    private double value;
    private OOPSourceCodeModel srcModel;

    public DITMetric(Component component, OOPSourceCodeModel srcModel) {
        this.srcModel = srcModel;
        this.value = calculateDit(component);
    }

    private int calculateDit(Component cmp) {
        if (cmp.componentType() == OOPSourceModelConstants.ComponentType.INTERFACE) {
            return 1;
        }

        int currentHighestScore = 1;

        for (ComponentInvocation cmpInv : cmp.componentInvocations(OOPSourceModelConstants.ComponentInvocations.EXTENSION)) {
            Optional<Component> invkCmp = this.srcModel.getComponent(cmpInv.invokedComponent());
            if (invkCmp.isPresent()) {
                int hierarchyScore = 1 + calculateDit(invkCmp.get());
                if (hierarchyScore > currentHighestScore) {
                    currentHighestScore = hierarchyScore;
                }
            }
        }
        return currentHighestScore;
    }

    public double value() {
        return this.value;
    }
}
