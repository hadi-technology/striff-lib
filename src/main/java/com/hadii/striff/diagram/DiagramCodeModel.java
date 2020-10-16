package com.hadii.striff.diagram;

import com.hadii.clarpse.sourcemodel.OOPSourceCodeModel;

import java.util.HashMap;
import java.util.Map;

/**
 * Decorates a {@link OOPSourceCodeModel} to facilitate generation of Stiff diagrams.
 */
public class DiagramCodeModel {

    private final Map<String, DiagramComponent> components = new HashMap<>();

    public DiagramCodeModel(OOPSourceCodeModel srcModel) {
        srcModel.components().forEach(value -> {
            DiagramComponent dCmp = new DiagramComponent(value, srcModel);
            this.components.put(dCmp.uniqueName(), dCmp);
        });
    }

    public DiagramCodeModel(DiagramComponent... components) {
        for (DiagramComponent component : components) {
            this.components.put(component.uniqueName(), component);
        }
    }

    public Map<String, DiagramComponent> components() {
        return components;
    }

    public DiagramComponent component(String componentName) {
        return components.get(componentName);
    }

    public boolean containsComponent(String key) {
        return components.containsKey(key);
    }

    public void addComponent(DiagramComponent component) {
        this.components.put(component.uniqueName(), component);
    }

    public DiagramCodeModel copy() {
        DiagramCodeModel copy = new DiagramCodeModel();
        this.components().forEach((key, value) -> {
            copy.addComponent(value);
        });
        return copy;
    }
}
