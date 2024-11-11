package com.hadii.striff.metrics;

import com.hadii.clarpse.sourcemodel.Component;
import com.hadii.clarpse.sourcemodel.OOPSourceCodeModel;

import java.util.Optional;

/**
 * Represents the Efferent Class Coupling metric, which is a measure of the
 * number of other classes referenced by a class.
 */
public class EfferentCouplingMetric implements Metric {

    private final OOPSourceCodeModel srcModel;

    public EfferentCouplingMetric(OOPSourceCodeModel srcModel) {
        this.srcModel = srcModel;
    }

    public double value(String cmpId) {
        Optional<Component> componentOpt = this.srcModel.getComponent(cmpId);
        if (componentOpt.isPresent()) {
            return componentOpt.get().references().size();
        } else {
            throw new IllegalArgumentException("Could not find component: " + cmpId + "!");
        }
    }

    @Override
    public String acronym() {
        return "EC";
    }
}
