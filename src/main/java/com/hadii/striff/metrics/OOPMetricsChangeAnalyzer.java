package com.hadii.striff.metrics;

import java.util.Optional;
import java.util.Set;
import org.checkerframework.checker.nullness.qual.NonNull;

import com.hadii.clarpse.sourcemodel.Component;
import com.hadii.clarpse.sourcemodel.OOPSourceCodeModel;
import com.hadii.striff.annotations.LogExecutionTime;

public class OOPMetricsChangeAnalyzer {
    private final OOPMetricsProfile oldProfile;
    private final OOPMetricsProfile updatedProfile;
    private Set<String> targetComponents;

    @LogExecutionTime
    public OOPMetricsChangeAnalyzer(OOPSourceCodeModel oldModel, OOPSourceCodeModel updatedModel,
            @NonNull Set<String> targetComponents) {
        this.oldProfile = new OOPMetricsProfile(oldModel, targetComponents);
        this.updatedProfile = new OOPMetricsProfile(updatedModel, targetComponents);
        this.targetComponents = targetComponents;
    }

    public Optional<MetricChange> analyzeChanges(String componentUniqueName) {
        Optional<Component> oldComponent = oldProfile.getSourceModel().getComponent(componentUniqueName);
        Optional<Component> updatedComponent = updatedProfile.getSourceModel().getComponent(componentUniqueName);

        if (!oldComponent.isPresent() && !updatedComponent.isPresent()) {
            throw new IllegalArgumentException(componentUniqueName + " does not exist!");
        }
        if (this.targetComponents != null && !this.targetComponents.isEmpty()
                && !this.targetComponents.contains(componentUniqueName)) {
            throw new IllegalArgumentException(componentUniqueName + " was not specified for analysis!");
        }

        double oldNOC = oldComponent.map(oldProfile::noc).orElse(0.0);
        double updatedNOC = updatedComponent.map(updatedProfile::noc).orElse(0.0);

        double oldDIT = oldComponent.map(oldProfile::dit).orElse(0.0);
        double updatedDIT = updatedComponent.map(updatedProfile::dit).orElse(0.0);

        double oldWMC = oldComponent.map(oldProfile::weightedMethodComplexity).orElse(0.0);
        double updatedWMC = updatedComponent.map(updatedProfile::weightedMethodComplexity).orElse(0.0);

        double oldAC = oldComponent.map(oldProfile::afferentCoupling).orElse(0.0);
        double updatedAC = updatedComponent.map(updatedProfile::afferentCoupling).orElse(0.0);

        double oldEC = oldComponent.map(oldProfile::efferentCoupling).orElse(0.0);
        double updatedEC = updatedComponent.map(updatedProfile::efferentCoupling).orElse(0.0);

        double oldEncapsulation = oldComponent.map(oldProfile::encapsulation).orElse(0.0);
        double updatedEncapsulation = updatedComponent.map(updatedProfile::encapsulation).orElse(0.0);

        return Optional.of(new MetricChange(
                componentUniqueName,
                oldNOC, updatedNOC,
                oldDIT, updatedDIT,
                oldWMC, updatedWMC,
                oldAC, updatedAC,
                oldEC, updatedEC,
                oldEncapsulation, updatedEncapsulation));
    }
}
