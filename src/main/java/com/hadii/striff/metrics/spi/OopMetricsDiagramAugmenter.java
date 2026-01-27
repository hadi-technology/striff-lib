package com.hadii.striff.metrics.spi;

import com.hadii.striff.diagram.DiagramComponent;
import com.hadii.striff.metrics.MetricChange;
import com.hadii.striff.metrics.OOPMetricsChangeAnalyzer;
import com.hadii.striff.parse.CodeDiff;
import com.hadii.striff.spi.DiagramAugmenter;

import java.util.Set;
import java.util.stream.Collectors;

public class OopMetricsDiagramAugmenter implements DiagramAugmenter {

    @Override
    public void augment(CodeDiff diff, Set<DiagramComponent> components) {
        if (components == null || components.isEmpty()) {
            return;
        }
        Set<String> targetComponents = components.stream()
                .map(DiagramComponent::uniqueName)
                .collect(Collectors.toSet());
        OOPMetricsChangeAnalyzer analyzer = new OOPMetricsChangeAnalyzer(
                diff.oldModel(), diff.newModel(), targetComponents);
        Set<String> addedComponents = diff.changeSet().addedComponents();
        Set<String> deletedComponents = diff.changeSet().deletedComponents();
        for (DiagramComponent component : components) {
            MetricChange change = analyzer.analyzeChanges(component.uniqueName()).orElse(null);
            if (change == null) {
                continue;
            }
            boolean isAdded = addedComponents.contains(component.uniqueName());
            boolean isDeleted = deletedComponents.contains(component.uniqueName());
            component.putAugmentation(OopMetricsAugmentation.KEY,
                    new OopMetricsAugmentation(change, isAdded, isDeleted));
        }
    }
}
