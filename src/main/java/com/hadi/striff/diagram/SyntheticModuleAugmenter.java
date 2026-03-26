package com.hadi.striff.diagram;

import com.hadi.clarpse.sourcemodel.OOPSourceCodeModel;
import com.hadi.clarpse.sourcemodel.Component;
import com.hadi.striff.ChangeSet;
import com.hadi.striff.parse.CodeDiff;
import com.hadi.striff.spi.DiagramAugmenter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashSet;
import java.util.Set;

/**
 * Augments the diagram model by adding synthetic modules for module-level functions and fields.
 * Module-level components (those without a parent class) are grouped into synthetic modules
 * based on their module attribute.
 *
 * <p>Only synthetic modules that are relevant to the current diff are added - i.e., modules
 * that contain added/modified/key-relation components, or module-level components that are
 * referenced by changed components. This prevents bloating diagrams with unrelated modules.
 *
 * <p>Relations from synthetic modules to their dependencies are created by
 * {@link com.hadi.striff.extractor.ExtractedRelationships}, so this augmenter only
 * adds the {@link DiagramComponent} instances for the synthetic modules themselves.
 */
public class SyntheticModuleAugmenter implements DiagramAugmenter {

    private static final Logger LOGGER = LogManager.getLogger(SyntheticModuleAugmenter.class);

    @Override
    public void augment(CodeDiff diff, Set<DiagramComponent> components) {
        OOPSourceCodeModel model = diff.mergedModel();
        ChangeSet changeSet = diff.changeSet();

        // Collect module keys that are relevant to this diff
        Set<String> relevantModuleKeys = findRelevantModuleKeys(model, changeSet, components);

        if (relevantModuleKeys.isEmpty()) {
            return;
        }

        LOGGER.debug("Adding {} relevant synthetic module components to diagram (filtered from {} total)",
                relevantModuleKeys.size(),
                SyntheticModuleSupport.syntheticComponentsByModule(model).size());

        // Create synthetic modules only for relevant modules
        SyntheticModuleSupport.syntheticComponentsByModule(model).forEach((moduleKey, synthetic) -> {
            if (relevantModuleKeys.contains(moduleKey)) {
                // Create a DiagramComponent for the synthetic module
                DiagramComponent syntheticDiagram = new DiagramComponent(synthetic, model);
                syntheticDiagram.putAugmentation("synthetic", true);
                syntheticDiagram.putAugmentation("syntheticDisplayName", moduleKey);

                // Add it to the components set
                components.add(syntheticDiagram);
            }
        });
    }

    /**
     * Finds module keys that are relevant to the current diff by checking:
     * 1. Modules containing added/modified/key-relation components
     * 2. Modules containing module-level components referenced by changed components
     *
     * @return set of relevant module keys
     */
    private Set<String> findRelevantModuleKeys(OOPSourceCodeModel model, ChangeSet changeSet,
                                                  Set<DiagramComponent> components) {
        Set<String> relevantModuleKeys = new HashSet<>();

        // Get all unique names of components involved in this diff (changed and displayed)
        Set<String> diffComponentNames = new HashSet<>();
        diffComponentNames.addAll(changeSet.addedComponents());
        diffComponentNames.addAll(changeSet.deletedComponents());
        diffComponentNames.addAll(changeSet.modifiedComponents());
        diffComponentNames.addAll(changeSet.keyRelationsComponents());

        // Also include components that are being displayed in the diagram
        for (DiagramComponent cmp : components) {
            diffComponentNames.add(cmp.uniqueName());
        }

        // Find modules containing changed components
        for (String componentName : diffComponentNames) {
            Component cmp = model.getComponent(componentName).orElse(null);
            if (cmp != null && cmp.module() != null && !cmp.module().trim().isEmpty()) {
                relevantModuleKeys.add(cmp.module());
            }
        }

        // Find modules containing module-level components referenced by changed components
        Set<String> processedModules = new HashSet<>();
        for (String componentName : diffComponentNames) {
            Component cmp = model.getComponent(componentName).orElse(null);
            if (cmp != null) {
                // Find all components this component references
                cmp.internalDependencies().forEach(ref -> {
                    Component referencedCmp = model.getComponent(ref.invokedComponent()).orElse(null);
                    if (referencedCmp != null
                            && SyntheticModuleSupport.isModuleLevelComponent(referencedCmp)
                            && referencedCmp.module() != null
                            && !referencedCmp.module().trim().isEmpty()) {
                        relevantModuleKeys.add(referencedCmp.module());
                    }
                });
            }
        }

        return relevantModuleKeys;
    }

    @Override
    public int order() {
        // Run early so other augmenters can work with the synthetic modules
        return 10;
    }
}
