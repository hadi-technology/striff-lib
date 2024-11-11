package com.hadii.striff.metrics;

import com.hadii.clarpse.reference.ComponentReference;
import com.hadii.clarpse.sourcemodel.Component;
import com.hadii.clarpse.sourcemodel.OOPSourceCodeModel;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Represents the Afferent Class Coupling metric, which is a measure of the
 * number of classes that reference the given class.
 */
public class AfferentCouplingMetric implements Metric {

    private final Map<String, Double> references = new HashMap<>();

    public AfferentCouplingMetric(OOPSourceCodeModel srcModel) {
        this.calculate(srcModel);
    }

    private void calculate(OOPSourceCodeModel srcModel) {
        for (Component tempCmp : srcModel.components().collect(Collectors.toList())) {
            if (tempCmp.componentType().isBaseComponent()) {
                for (ComponentReference cmpRef : tempCmp
                        .references()) {
                    references.put(cmpRef.invokedComponent(),
                            references.getOrDefault(cmpRef.invokedComponent(), 0.0) + 1);
                }
            }
        }
    }

    public double value(String cmpId) {
        return this.references.getOrDefault(cmpId, 0.0);
    }

    @Override
    public String acronym() {
        return "AC";
    }
}
