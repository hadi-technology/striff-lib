package com.hadii.striff.metrics;

import com.hadii.clarpse.sourcemodel.Component;
import com.hadii.clarpse.sourcemodel.OOPSourceCodeModel;

import java.util.HashSet;
import java.util.Set;

public class OOPMetricsProfile {
    private final OOPMetricsIndex index;

    public OOPMetricsProfile(OOPSourceCodeModel srcModel, Set<String> targetComponents) {
        this.index = new OOPMetricsIndex(srcModel, targetComponents);
    }

    public OOPMetricsProfile(OOPSourceCodeModel srcModel) {
        this(srcModel, new HashSet<String>());
    }

    public double noc(Component cmp) {
        validateCmp(cmp);
        return index.getNOC(cmp.uniqueName());
    }

    public double dit(Component cmp) {
        validateCmp(cmp);
        return index.getDIT(cmp.uniqueName());
    }

    public double weightedMethodComplexity(Component cmp) {
        validateCmp(cmp);
        return index.getWMC(cmp.uniqueName());
    }

    public double afferentCoupling(Component cmp) {
        validateCmp(cmp);
        return index.getAfferentCoupling(cmp.uniqueName());
    }

    public double efferentCoupling(Component cmp) {
        validateCmp(cmp);
        return index.getEfferentCoupling(cmp.uniqueName());
    }

    public double encapsulation(Component cmp) {
        validateCmp(cmp);
        return index.getEncapsulation(cmp.uniqueName());
    }

    public OOPSourceCodeModel getSourceModel() {
        return this.index.getSourceModel();
    }

    private void validateCmp(Component cmp) {
        if (!this.index.getSourceModel().containsComponent(cmp.uniqueName())) {
            throw new IllegalArgumentException("Component: " + cmp.componentName() + " was not found!");
        }
        if (!cmp.componentType().isBaseComponent()) {
            throw new IllegalArgumentException("OOP Metrics may only be generated for base component types!");
        }
    }
}
