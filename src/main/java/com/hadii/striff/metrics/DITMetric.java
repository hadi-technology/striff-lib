package com.hadii.striff.metrics;

import com.hadii.clarpse.reference.ComponentReference;
import com.hadii.clarpse.sourcemodel.Component;
import com.hadii.clarpse.sourcemodel.OOPSourceCodeModel;
import com.hadii.clarpse.sourcemodel.OOPSourceModelConstants;

import java.util.Optional;

/**
 * Represents a Depth of Inheritance metric, which is a measure of how far down a class is declared in
 * the inheritance hierarchy.
 */
public class DITMetric implements Metric {

    private final double value;
    private final OOPSourceCodeModel srcModel;

    public DITMetric(Component component, OOPSourceCodeModel srcModel) {
        this.srcModel = srcModel;
        this.value = calculateDit(component);
    }

    private int calculateDit(Component cmp) {
        if (cmp.componentType() == OOPSourceModelConstants.ComponentType.INTERFACE) {
            return 1;
        }

        int currentHighestScore = 1;

        for (ComponentReference cmpRef : cmp.references(OOPSourceModelConstants.TypeReferences.EXTENSION)) {
            Optional<Component> invkCmp = this.srcModel.getComponent(cmpRef.invokedComponent());
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
