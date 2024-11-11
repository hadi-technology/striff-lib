package com.hadii.striff.metrics;

import com.hadii.clarpse.sourcemodel.Component;
import com.hadii.clarpse.sourcemodel.OOPSourceCodeModel;
import com.hadii.clarpse.sourcemodel.OOPSourceModelConstants;

import java.util.Optional;

/**
 * Represents a Depth of Inheritance metric, which is a measure of how far down
 * a class is declared in the inheritance hierarchy.
 */
public class DITMetric implements Metric {

    private final OOPSourceCodeModel srcModel;

    public DITMetric(OOPSourceCodeModel srcModel) {
        this.srcModel = srcModel;
    }

    private Double calculateDit(String cmpId) {
        double currentHighestScore = 1;
        Optional<Component> componentOpt = this.srcModel.getComponent(cmpId);
        // Calculate the highest score if the component is present and not an interface
        if (componentOpt.isPresent()
                && componentOpt.get().componentType() != OOPSourceModelConstants.ComponentType.INTERFACE) {
            currentHighestScore = componentOpt.get()
                    .references(OOPSourceModelConstants.TypeReferences.EXTENSION).stream()
                    .map(cmpRef -> this.srcModel.getComponent(cmpRef.invokedComponent()))
                    .filter(Optional::isPresent)
                    .mapToDouble(invkCmp -> 1.0 + calculateDit(invkCmp.get().uniqueName()))
                    .max()
                    .orElse(currentHighestScore);
        }
        return currentHighestScore;
    }

    public double value(String cmpUniqueName) {
        return this.calculateDit(cmpUniqueName);
    }

    @Override
    public String acronym() {
        return "DIT";
    }
}
