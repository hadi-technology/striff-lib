package com.hadii.stiff.metrics;

import com.hadii.clarpse.reference.ComponentReference;
import com.hadii.clarpse.sourcemodel.Component;
import com.hadii.clarpse.sourcemodel.OOPSourceCodeModel;
import com.hadii.clarpse.sourcemodel.OOPSourceModelConstants;

import java.util.stream.Collectors;

/**
 * Represents a Number of Children metric, which is a measure of the the number of direct
 * subclasses of a class.
 */
public class NOCMetric implements Metric {

    private final Component component;
    private final OOPSourceCodeModel srcModel;

    public NOCMetric(Component component, OOPSourceCodeModel srcModel) {
        this.component = component;
        this.srcModel = srcModel;
    }

    public double value() {
        String currUniqueName = this.component.uniqueName();
        int noc = 0;
        if (this.component.componentType().isBaseComponent()) {
            for (Component tempCmp : srcModel.components().collect(Collectors.toList())) {
                if (tempCmp.componentType().isBaseComponent()) {
                    for (ComponentReference cmpRef : tempCmp.references(OOPSourceModelConstants.TypeReferences.EXTENSION)) {
                        if (cmpRef.invokedComponent().equals(currUniqueName)) {
                            noc += 1;
                        }
                    }
                }
            }
        }
        return noc;
    }
}
