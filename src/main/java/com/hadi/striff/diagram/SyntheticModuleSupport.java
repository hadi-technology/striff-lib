package com.hadi.striff.diagram;

import com.hadi.clarpse.sourcemodel.Component;
import com.hadi.clarpse.sourcemodel.OOPSourceCodeModel;
import com.hadi.clarpse.sourcemodel.OOPSourceModelConstants;
import com.hadi.clarpse.sourcemodel.Package;
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

    /**
     * Whether a unique name names a synthetic module.
     *
     * <p>A synthetic module is held under its package path, then {@code module:}, then the module's
     * own name -- {@code src.orders.module:update} -- or, for a module in the root package, under
     * {@code module:update} alone.
     *
     * @param uniqueName the unique name to check
     * @return true if the name is a synthetic module's
     */
    public static boolean isSyntheticUniqueName(String uniqueName) {
        if (uniqueName == null) {
            return false;
        }
        return uniqueName.startsWith(SYNTHETIC_PREFIX) || uniqueName.contains("." + SYNTHETIC_PREFIX);
    }

    /**
     * Checks if the given component is a synthetic module.
     * A synthetic module is a fabricated class-like component that acts as a container
     * for module-level functions and fields.
     *
     * @param component the component to check
     * @return true if the component is a synthetic module
     */
    public static boolean isSyntheticModule(Component component) {
        return component != null && isSyntheticUniqueName(component.componentName());
    }

    /**
     * The key identifying the module a module-level component belongs to.
     *
     * <p>Qualified by the component's package, because a module's own name is a bare file name in
     * every language that has module-level members. Keyed by that name alone, two files called
     * {@code update.ts} in different directories are one module, drawn in one package and holding
     * both files' members, and which package that is depends on which file was seen first.
     *
     * @param component the module-level component
     * @return the key of the module holding it, such as {@code src.orders.update}
     */
    public static String moduleKey(Component component) {
        if (component == null) {
            throw new IllegalStateException("Module-level component is null.");
        }
        String module = component.module();
        if (module == null || module.trim().isEmpty()) {
            throw new IllegalStateException("Module-level component has no module name: " + component.uniqueName());
        }
        String packagePath = ComponentHelper.packagePath(component.pkg());
        if (packagePath.isEmpty()) {
            return module.trim();
        }
        return packagePath + "." + module.trim();
    }

    /**
     * The unique name a synthetic module is held under.
     *
     * @param packagePath the module's package path, dot separated, or empty for the root package
     * @param moduleName the module's own name
     * @return {@code src.orders.module:update}, or {@code module:update} in the root package
     */
    public static String syntheticUniqueName(String packagePath, String moduleName) {
        String prefixedName = SYNTHETIC_PREFIX + moduleName;
        if (packagePath == null || packagePath.isEmpty()) {
            return prefixedName;
        }
        return packagePath + "." + prefixedName;
    }

    /**
     * The unique name of a synthetic module in the root package.
     *
     * @param moduleName the module's own name
     * @return {@code module:update}
     */
    public static String syntheticUniqueName(String moduleName) {
        return syntheticUniqueName("", moduleName);
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
            // Every member under one key now comes from one file, so any of them carries the
            // module's own name; the key carries the package it sits in.
            Component synthetic = syntheticComponent(moduleName(model, entry.getValue(), moduleKey),
                    entry.getValue());
            String sourceFile = moduleSourceFile(model, entry.getValue());
            if (sourceFile != null && !sourceFile.trim().isEmpty()) {
                synthetic.setSourceFilePath(sourceFile);
            }
            Package pkg = modulePackage(model, entry.getValue());
            if (pkg != null) {
                synthetic.setPkg(pkg);
            }
            // A module's members all come from one file, so they are boundary components together.
            synthetic.setBoundary(anyBoundary(model, entry.getValue()));
            syntheticByModule.put(moduleKey, synthetic);
        }
        return syntheticByModule;
    }

    /**
     * Builds the synthetic module holding the given members.
     *
     * <p>Takes the module's own name, not its key: the package is carried by the component's own
     * package, which the caller sets, and which qualifies the unique name. The name stays the
     * short one so a reader sees the file's name rather than a path.
     *
     * @param moduleName the module's own name, such as {@code update}
     * @param children the unique names of the members it holds
     * @return the synthetic module component
     */
    public static Component syntheticComponent(String moduleName, Collection<String> children) {
        Component synthetic = new Component();
        synthetic.setComponentType(OOPSourceModelConstants.ComponentType.CLASS);
        synthetic.setComponentName(SYNTHETIC_PREFIX + moduleName);
        synthetic.setName(moduleName);
        synthetic.setValue(SYNTHETIC_PREFIX + moduleName);
        synthetic.setModule(moduleName);
        if (children != null) {
            children.forEach(synthetic::insertChildComponent);
        }
        return synthetic;
    }

    private static boolean anyBoundary(OOPSourceCodeModel model, Collection<String> children) {
        for (String childName : children) {
            Component child = model.component(childName).orElse(null);
            if (child != null && child.isBoundary()) {
                return true;
            }
        }
        return false;
    }

    /**
     * The module's own name, read off any of its members, falling back to the key itself.
     */
    private static String moduleName(OOPSourceCodeModel model, Collection<String> children, String moduleKey) {
        if (model == null || children == null) {
            return moduleKey;
        }
        for (String childName : children) {
            Component child = model.component(childName).orElse(null);
            if (child == null) {
                continue;
            }
            String module = child.module();
            if (module != null && !module.trim().isEmpty()) {
                return module.trim();
            }
        }
        return moduleKey;
    }

    private static String moduleSourceFile(OOPSourceCodeModel model, Collection<String> children) {
        if (model == null || children == null || children.isEmpty()) {
            return null;
        }
        for (String childName : children) {
            // Read-only, and the value read out is immutable: a copy would hand back the same
            // String this does. Runs once per child of every module in the model.
            Component child = model.component(childName).orElse(null);
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

    private static Package modulePackage(OOPSourceCodeModel model, Collection<String> children) {
        if (model == null || children == null || children.isEmpty()) {
            return null;
        }
        for (String childName : children) {
            // As above, and provably identical here: Package is immutable and a copied component
            // shares its owner's instance of it, so the copy never differed in the first place.
            Component child = model.component(childName).orElse(null);
            if (child == null) {
                continue;
            }
            Package pkg = child.pkg();
            if (pkg != null) {
                return pkg;
            }
        }
        return null;
    }
}
