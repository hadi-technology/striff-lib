package com.hadi.striff.diagram;

import com.hadi.clarpse.sourcemodel.OOPSourceCodeModel;
import com.hadi.striff.parse.CodeDiff;
import com.hadi.striff.spi.DiagramAugmenter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Set;

/**
 * Augments the diagram model by adding synthetic modules for module-level functions and fields.
 * Module-level components (those without a parent class) are grouped into synthetic modules
 * based on their module attribute.
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

        // Find all module-level components that need synthetic modules
        long moduleLevelCount = model.components()
                .filter(SyntheticModuleSupport::isModuleLevelComponent)
                .count();

        if (moduleLevelCount == 0) {
            return;
        }

        LOGGER.debug("Adding {} synthetic module components to diagram",
                SyntheticModuleSupport.syntheticComponentsByModule(model).size());

        // Create synthetic modules for each unique module key
        // Relations from synthetic modules are already created by ExtractedRelationships
        SyntheticModuleSupport.syntheticComponentsByModule(model).forEach((moduleKey, synthetic) -> {
            // Create a DiagramComponent for the synthetic module
            DiagramComponent syntheticDiagram = new DiagramComponent(synthetic, model);
            syntheticDiagram.putAugmentation("synthetic", true);
            syntheticDiagram.putAugmentation("syntheticDisplayName", moduleKey);

            // Add it to the components set
            components.add(syntheticDiagram);
        });
    }

    @Override
    public int order() {
        // Run early so other augmenters can work with the synthetic modules
        return 10;
    }
}
