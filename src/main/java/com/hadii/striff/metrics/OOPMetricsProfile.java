package com.hadii.striff.metrics;

import com.hadii.clarpse.sourcemodel.Component;
import com.hadii.clarpse.sourcemodel.OOPSourceCodeModel;

/**
 * A Profile of various OOP related Metrics.
 */
public class OOPMetricsProfile {

    private final Component cmp;
    private final OOPSourceCodeModel srcModel;

    public OOPMetricsProfile(Component component, OOPSourceCodeModel srcModel) {
        if (!component.componentType().isBaseComponent()) {
            throw new IllegalArgumentException("OOP Metrics may only be generated for base component types!");
        }
        this.cmp = component;
        this.srcModel = srcModel;
    }

    public double noc() {
        return new NOCMetric(this.cmp, this.srcModel).value();
    }

    public double dit() {
        return new DITMetric(this.cmp, this.srcModel).value();
    }

    public double cyclo() {
        return new CycloMetric(this.cmp).value();
    }

    public double fanout() {
        return new FanOutMetric(this.cmp).value();
    }

    public double fanin() {
        return new FanInMetric(this.srcModel, this.cmp).value();
    }
}
