package com.hadii.striff.diagram;

import com.hadii.clarpse.sourcemodel.OOPSourceCodeModel;
import com.hadii.striff.StriffDiagram;

import java.util.HashMap;
import java.util.Map;

/**
 * Decorates a {@link OOPSourceCodeModel} to facilitate generation
 * of a {@link StriffDiagram}.
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

    public Map<String, DiagramComponent> components() {
        return components;
    }

    public DiagramComponent component(String s) {
        return components.get(s);
    }

    public boolean containsComponent(String key) {
        return components.containsKey(key);
    }

    public void addComponent(DiagramComponent component) {
        this.components.put(component.uniqueName(), component);
    }
}
