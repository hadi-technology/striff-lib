package com.hadii.striff.metrics;

import com.hadii.clarpse.sourcemodel.Component;
import com.hadii.clarpse.sourcemodel.OOPSourceCodeModel;

/**
 * A Profile of various OOP related Metrics.
 */
public class OOPMetricsProfile {

    private final OOPSourceCodeModel srcModel;
    private final NOCMetric nocMetric;
    private final DITMetric ditMetric;
    private final WeightedMethodComplexity weightedMethodComplexityMetric;
    private final AfferentCouplingMetric afferentCouplingMetric;
    private EncapsulationMetric encapsulationMetric;
    private EfferentCouplingMetric efferentCouplingMetric;

    public OOPMetricsProfile(OOPSourceCodeModel srcModel) {
        this.srcModel = srcModel;
        this.nocMetric = new NOCMetric(srcModel);
        this.ditMetric = new DITMetric(srcModel);
        this.weightedMethodComplexityMetric = new WeightedMethodComplexity(srcModel);
        this.afferentCouplingMetric = new AfferentCouplingMetric(srcModel);
        this.efferentCouplingMetric = new EfferentCouplingMetric(srcModel);
        this.encapsulationMetric = new EncapsulationMetric(srcModel);
    }

    private void validateCmp(Component cmp) {
        if (!this.srcModel.containsComponent(cmp.uniqueName())) {
            throw new IllegalArgumentException("Component: " + cmp.componentName() + " was not found!");
        }
        if (!cmp.componentType().isBaseComponent()) {
            throw new IllegalArgumentException("OOP Metrics may only be generated for base component types!");
        }
    }

    public double noc(Component cmp) {
        this.validateCmp(cmp);
        return this.nocMetric.value(cmp.uniqueName());
    }

    public double dit(Component cmp) {
        this.validateCmp(cmp);
        return this.ditMetric.value(cmp.uniqueName());
    }

    public double weightedMethodComplexity(Component cmp) {
        this.validateCmp(cmp);
        return this.weightedMethodComplexityMetric.value(cmp.uniqueName());
    }

    public double afferentCoupling(Component cmp) {
        this.validateCmp(cmp);
        return this.afferentCouplingMetric.value(cmp.uniqueName());
    }

    public double efferentCoupling(Component cmp) {
        this.validateCmp(cmp);
        return this.efferentCouplingMetric.value(cmp.uniqueName());
    }

    public double encapsulation(Component cmp) {
        this.validateCmp(cmp);
        return this.encapsulationMetric.value(cmp.uniqueName());
    }
}
