package com.hadii.striff.metrics;

import com.hadii.clarpse.sourcemodel.Component;
import com.hadii.clarpse.sourcemodel.OOPSourceCodeModel;
import com.hadii.clarpse.sourcemodel.OOPSourceModelConstants.ComponentType;

/**
 * Represents the Average Cyclomatic Complexity metric, which is a measure of
 * the average method complexity of a given an object.
 */
public class WeightedMethodComplexity implements Metric {

    private OOPSourceCodeModel srcModel;

    public WeightedMethodComplexity(OOPSourceCodeModel srcModel) {
        this.srcModel = srcModel;
    }

    public double value(String cmpId) {
        if (srcModel.containsComponent(cmpId)) {
            int weightedMethodComplexity = 0;
            Component currComponent = srcModel.getComponent(cmpId).get();
            // Iterate over children to count private/protected modifiers
            for (String child : currComponent.children()) {
                if (srcModel.containsComponent(child)) {
                    Component childCmp = srcModel.getComponent(child).get();
                    if (childCmp.componentType() == ComponentType.METHOD) {
                        weightedMethodComplexity += childCmp.cyclo();
                    }
                }
            }
            return weightedMethodComplexity;
        } else {
            throw new IllegalArgumentException("Could not find component: " + cmpId + "!");
        }
    }

    @Override
    public String acronym() {
        return "WMC";
    }
}
