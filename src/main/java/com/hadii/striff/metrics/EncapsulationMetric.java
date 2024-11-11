package com.hadii.striff.metrics;

import com.hadii.clarpse.sourcemodel.Component;
import com.hadii.clarpse.sourcemodel.OOPSourceCodeModel;

/**
 * The Encapsulation Ratio measures how well a class hides its data,
 * focusing on the accessibility of its fields and methods. A higher
 * encapsulation ratio suggests better data hiding, which is a core
 * principle of OOP.
 */
public class EncapsulationMetric implements Metric {

    private OOPSourceCodeModel srcModel;

    public EncapsulationMetric(OOPSourceCodeModel srcModel) {
        this.srcModel = srcModel;
    }

    public double value(String cmpId) {
        if (srcModel.containsComponent(cmpId)) {
            int privateMembers = 0;
            Component currComponent = srcModel.getComponent(cmpId).get();
            // Iterate over children to count private/protected modifiers
            for (String child : currComponent.children()) {
                if (srcModel.containsComponent(child)) {
                    Component childCmp = srcModel.getComponent(child).get();
                    if (childCmp.modifiers().contains("private") || childCmp.modifiers().contains("protected")) {
                        privateMembers++;
                    }
                }
            }
            // Avoid division by zero if there are no children
            int totalChildren = currComponent.children().size();
            if (totalChildren == 0) {
                return 0.0;
            } else {
                return (double) privateMembers / totalChildren;
            }
        } else {
            throw new IllegalArgumentException("Could not find component: " + cmpId + "!");
        }
    }

    @Override
    public String acronym() {
        return "ENC";
    }
}
