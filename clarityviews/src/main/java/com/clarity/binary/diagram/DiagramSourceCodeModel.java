package com.clarity.binary.diagram;

import com.clarity.sourcemodel.OOPSourceCodeModel;

import java.util.HashMap;
import java.util.Map;

/**
 * Decorates Clarpse's {@link com.clarity.sourcemodel.OOPSourceCodeModel} to facilitate generation
 * of {@link com.clarity.binary.diagram.view.SDView}'s using our custom {@link DiagramComponent}'s.
 */
public class DiagramSourceCodeModel {

    private final Map<String, DiagramComponent> components;

    public DiagramSourceCodeModel(OOPSourceCodeModel srcModel) {
        Map<String, DiagramComponent> newCmps = new HashMap<>();
        srcModel.components().forEach(value -> {
            DiagramComponent dCmp = new DiagramComponent(value, srcModel);
            newCmps.put(dCmp.uniqueName(), dCmp);
        });
        this.components = newCmps;
    }

    public Map<String, DiagramComponent> getComponents() {
        return components;
    }

    public DiagramComponent getComponent(String s) {
        return components.get(s);
    }

    public boolean containsComponent(String key) {
        return components.containsKey(key);
    }
}
