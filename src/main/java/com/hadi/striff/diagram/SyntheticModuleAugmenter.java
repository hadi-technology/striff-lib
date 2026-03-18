package com.hadi.striff.diagram;

import com.hadi.clarpse.reference.ComponentReference;
import com.hadi.clarpse.sourcemodel.Component;
import com.hadi.clarpse.sourcemodel.OOPSourceCodeModel;
import com.hadi.clarpse.sourcemodel.OOPSourceModelConstants.ComponentType;
import com.hadi.striff.parse.CodeDiff;
import com.hadi.striff.spi.DiagramAugmenter;
import com.hadi.striff.extractor.ComponentAssociationMultiplicity;
import com.hadi.striff.extractor.ComponentRelation;
import com.hadi.striff.extractor.DiagramConstants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Augments the diagram model by adding synthetic modules for module-level functions and fields.
 * Module-level components (those without a parent class) are grouped into synthetic modules
 * based on their module attribute.
 */
public class SyntheticModuleAugmenter implements DiagramAugmenter {

    private static final Logger LOGGER = LogManager.getLogger(SyntheticModuleAugmenter.class);

    @Override
    public void augment(CodeDiff diff, Set<DiagramComponent> components) {
        OOPSourceCodeModel model = diff.mergedModel();

        // Find all module-level components that need synthetic modules
        Set<Component> moduleLevelComponents = model.components()
                .filter(SyntheticModuleSupport::isModuleLevelComponent)
                .collect(Collectors.toSet());

        if (moduleLevelComponents.isEmpty()) {
            return;
        }

        LOGGER.info("Found {} module-level components, creating synthetic modules", moduleLevelComponents.size());

        // Create synthetic modules for each unique module key
        SyntheticModuleSupport.syntheticComponentsByModule(model).forEach((moduleKey, synthetic) -> {
            // Create a DiagramComponent for the synthetic module
            DiagramComponent syntheticDiagram = new DiagramComponent(synthetic, model);
            syntheticDiagram.putAugmentation("synthetic", true);
            syntheticDiagram.putAugmentation("syntheticDisplayName", moduleKey);

            // Add it to the components set
            components.add(syntheticDiagram);

            // Create relations from module-level components to the synthetic module
            createModuleLevelRelations(diff, synthetic, moduleKey, model);

            LOGGER.debug("Added synthetic module for: {}", moduleKey);
        });
    }

    /**
     * Creates relations from synthetic modules to targets that module-level components reference.
     * For example, if a module-level function "util.topLevelFn" has a reference to "Animal",
     * this creates a relation from "module:util" to "Animal".
     */
    private void createModuleLevelRelations(CodeDiff diff, Component synthetic,
            String moduleKey, OOPSourceCodeModel model) {
        Set<Component> moduleLevelComponents = model.components()
                .filter(SyntheticModuleSupport::isModuleLevelComponent)
                .filter(cmp -> moduleKey.equals(cmp.module()))
                .collect(Collectors.toSet());

        LOGGER.info("Creating relations for synthetic module {} from {} module-level components",
                moduleKey, moduleLevelComponents.size());

        for (Component moduleLevelCmp : moduleLevelComponents) {
            // Get all references from this module-level component
            Set<ComponentReference> references = new LinkedHashSet<>(moduleLevelCmp.internalDependencies());

            LOGGER.info("Processing module-level component {} with {} references",
                    moduleLevelCmp.uniqueName(), references.size());

            for (ComponentReference ref : references) {
                LOGGER.info("Processing reference to: {}", ref.invokedComponent());

                // Resolve the target component
                if (!model.containsComponent(ref.invokedComponent())) {
                    continue;
                }

                Component target = model.getComponent(ref.invokedComponent()).orElse(null);
                if (target == null) {
                    continue;
                }

                // If target is not a base component, try to get its parent
                if (!target.componentType().isBaseComponent()) {
                    try {
                        target = model.parentBaseCmp(target.uniqueName());
                    } catch (IllegalArgumentException e) {
                        LOGGER.debug("No parent base component for reference target: {}", ref.invokedComponent());
                        continue;
                    }
                }

                if (target == null || target.equals(synthetic)) {
                    continue;
                }

                // Determine the association type based on the module-level component type
                DiagramConstants.ComponentAssociation associationType;
                if (moduleLevelCmp.componentType() == ComponentType.FIELD
                        || moduleLevelCmp.componentType() == ComponentType.MODULE_FIELD) {
                    // Use COMPOSITION for fields (similar to how class fields are treated)
                    associationType = DiagramConstants.ComponentAssociation.COMPOSITION;
                } else {
                    // Use WEAK_ASSOCIATION for methods and functions
                    associationType = DiagramConstants.ComponentAssociation.WEAK_ASSOCIATION;
                }

                // Create the relation from synthetic module to target
                ComponentRelation relation = new ComponentRelation();
                try {
                    // Use reflection or direct field access to bypass validation
                    setRelationField(relation, "originalComponent", synthetic);
                    setRelationField(relation, "targetComponent", target);
                    setRelationField(relation, "targetComponentRelationMultiplicity",
                            new ComponentAssociationMultiplicity(DiagramConstants.DefaultClassMultiplicities.NONE));
                    setRelationField(relation, "associationType", associationType);

                    diff.extractedRels().insertRelation(relation);

                    LOGGER.info("Created relation from {} to {}",
                            synthetic.uniqueName(), target.uniqueName());
                } catch (Exception e) {
                    LOGGER.error("Failed to create relation from synthetic module: {}", e.getMessage());
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void setRelationField(Object obj, String fieldName, Object value) throws Exception {
        java.lang.reflect.Field field = obj.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(obj, value);
    }

    @Override
    public int order() {
        // Run early so other augmenters can work with the synthetic modules
        return 10;
    }
}
