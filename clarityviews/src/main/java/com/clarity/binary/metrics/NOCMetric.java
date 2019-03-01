package com.clarity.binary.metrics;

import com.clarity.invocation.ComponentInvocation;
import com.clarity.sourcemodel.Component;
import com.clarity.sourcemodel.OOPSourceCodeModel;
import com.clarity.sourcemodel.OOPSourceModelConstants;

import java.util.stream.Collectors;

/**
 * Represents a Number of Children metric, which is a measure of the the number of direct
 * subclasses of a class.
 */
public class NOCMetric implements Metric {

    private Component component;
    private OOPSourceCodeModel srcModel;

    public NOCMetric(Component component, OOPSourceCodeModel srcModel) {
        this.component = component;
        this.srcModel = srcModel;
    }

    public double value() {
        String currUniqueName = this.component.uniqueName();
        int noc = 0;
        if (this.component.componentType().isBaseComponent()) {
            for (com.clarity.sourcemodel.Component tempCmp : srcModel.components().collect(Collectors.toList())) {
                if (tempCmp.componentType().isBaseComponent()) {
                    for (ComponentInvocation cmpInv : tempCmp.componentInvocations(OOPSourceModelConstants.ComponentInvocations.EXTENSION)) {
                        if (cmpInv.invokedComponent().equals(currUniqueName)) {
                            noc += 1;
                        }
                    }
                }
            }
        }
        return noc;
    }
}
