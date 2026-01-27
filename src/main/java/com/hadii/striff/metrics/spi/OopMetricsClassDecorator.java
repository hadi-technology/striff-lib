package com.hadii.striff.metrics.spi;

import com.hadii.striff.diagram.DiagramComponent;
import com.hadii.striff.diagram.display.DiagramDisplay;
import com.hadii.striff.diagram.display.MetricBadges;
import com.hadii.striff.metrics.MetricChange;
import com.hadii.striff.spi.ClassDecorator;
import com.hadii.striff.spi.ClassInsertionPoint;
import java.util.List;
import java.util.Optional;

public class OopMetricsClassDecorator implements ClassDecorator {

    @Override
    public ClassInsertionPoint insertionPoint() {
        return ClassInsertionPoint.TOP;
    }

    @Override
    public List<String> decorateClass(DiagramComponent component, DiagramDisplay display) {
        Optional<Object> augmentation = component.augmentation(OopMetricsAugmentation.KEY);
        if (augmentation.isEmpty() || !(augmentation.get() instanceof OopMetricsAugmentation)) {
            return List.of();
        }
        OopMetricsAugmentation metricsAug = (OopMetricsAugmentation) augmentation.get();
        MetricChange metricChange = metricsAug.metricChange();
        try {
            String badges = new MetricBadges(display.colorScheme()).metricBadges(
                    metricChange, metricsAug.deleted(), metricsAug.added());
            return List.of(badges);
        } catch (Exception e) {
            return List.of("---\n");
        }
    }
}
