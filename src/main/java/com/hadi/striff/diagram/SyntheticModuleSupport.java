package com.hadi.striff.diagram;

import com.hadi.clarpse.sourcemodel.Component;
import com.hadi.clarpse.sourcemodel.OOPSourceCodeModel;
import com.hadi.clarpse.sourcemodel.OOPSourceModelConstants;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class SyntheticModuleSupport {

    public static final String SYNTHETIC_PREFIX = "module:";

    private SyntheticModuleSupport() {
    }

    public static boolean isModuleLevelComponent(Component component) {
        if (component == null) {
            return false;
        }
        OOPSourceModelConstants.ComponentType type = component.componentType();
        return type == OOPSourceModelConstants.ComponentType.FUNCTION
                || type == OOPSourceModelConstants.ComponentType.MODULE_FIELD;
    }

    public static boolean isSyntheticComponent(DiagramComponent component) {
        return component != null && isSyntheticUniqueName(component.uniqueName());
    }

    public static boolean isSyntheticUniqueName(String uniqueName) {
        return uniqueName != null && uniqueName.startsWith(SYNTHETIC_PREFIX);
    }

    public static String moduleKey(Component component) {
        if (component == null) {
            throw new IllegalStateException("Module-level component is null.");
        }
        String module = component.module();
        if (module != null && !module.trim().isEmpty()) {
            return module.trim();
        }
        throw new IllegalStateException("Module-level component has no module name: " + component.uniqueName());
    }

    public static String syntheticUniqueName(String moduleKey) {
        return SYNTHETIC_PREFIX + moduleKey;
    }

    public static Map<String, Set<String>> moduleChildren(OOPSourceCodeModel model) {
        Map<String, Set<String>> children = new HashMap<>();
        model.components().filter(SyntheticModuleSupport::isModuleLevelComponent).forEach(component -> {
            String moduleKey = moduleKey(component);
            children.computeIfAbsent(moduleKey, key -> new HashSet<>()).add(component.uniqueName());
        });
        return children;
    }

    public static Map<String, Component> syntheticComponentsByModule(OOPSourceCodeModel model) {
        Map<String, Set<String>> childrenByModule = moduleChildren(model);
        Map<String, Component> syntheticByModule = new HashMap<>();
        for (Map.Entry<String, Set<String>> entry : childrenByModule.entrySet()) {
            String moduleKey = entry.getKey();
            Component synthetic = syntheticComponent(moduleKey, entry.getValue());
            String sourceFile = moduleSourceFile(model, entry.getValue());
            if (sourceFile != null && !sourceFile.trim().isEmpty()) {
                synthetic.setSourceFilePath(sourceFile);
            }
            syntheticByModule.put(moduleKey, synthetic);
        }
        return syntheticByModule;
    }

    public static Component syntheticComponent(String moduleKey, Collection<String> children) {
        Component synthetic = new Component();
        synthetic.setComponentType(OOPSourceModelConstants.ComponentType.CLASS);
        synthetic.setComponentName(syntheticUniqueName(moduleKey));
        synthetic.setName(moduleKey);
        synthetic.setValue(syntheticUniqueName(moduleKey));
        synthetic.setModule(moduleKey);
        if (children != null) {
            children.forEach(synthetic::insertChildComponent);
        }
        return synthetic;
    }

    private static String moduleSourceFile(OOPSourceCodeModel model, Collection<String> children) {
        if (model == null || children == null || children.isEmpty()) {
            return null;
        }
        for (String childName : children) {
            Component child = model.getComponent(childName).orElse(null);
            if (child == null) {
                continue;
            }
            String sourceFile = child.sourceFile();
            if (sourceFile != null && !sourceFile.trim().isEmpty()) {
                return sourceFile;
            }
        }
        return null;
    }
}
