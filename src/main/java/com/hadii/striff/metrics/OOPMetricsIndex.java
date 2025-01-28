package com.hadii.striff.metrics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.checkerframework.checker.nullness.qual.NonNull;

import com.hadii.clarpse.reference.ComponentReference;
import com.hadii.clarpse.sourcemodel.Component;
import com.hadii.clarpse.sourcemodel.OOPSourceCodeModel;
import com.hadii.clarpse.sourcemodel.OOPSourceModelConstants.ComponentType;
import com.hadii.clarpse.sourcemodel.OOPSourceModelConstants.TypeReferences;

public class OOPMetricsIndex {
    private final OOPSourceCodeModel srcModel;
    private final Set<String> targetComponents;

    // Afferent coupling (Ca): how many components reference this one
    private final Map<String, Double> afferentReferences = new HashMap<>();

    // Efferent coupling (Ce): how many references this component makes
    private final Map<String, Double> efferentReferences = new HashMap<>();

    // Weighted Method Complexity (WMC)
    private final Map<String, Double> wmcValues = new HashMap<>();

    // Encapsulation ratio
    private final Map<String, Double> encapsulationValues = new HashMap<>();

    // Depth of Inheritance Tree (DIT)
    private final Map<String, Double> ditValues = new HashMap<>(); // Memoized
    private final Set<String> ditComputed = new HashSet<>(); // To track which IDs are fully computed

    // Number of Children (NOC)
    private final Map<String, Double> subclasses = new HashMap<>();
    private final Map<String, List<String>> childrenMap = new HashMap<>();

    public OOPMetricsIndex(OOPSourceCodeModel srcModel, @NonNull Set<String> targetComponents) {
        this.srcModel = srcModel;
        this.targetComponents = targetComponents;
        // 1) Compute afferent coupling (Ca) for ALL components
        computeMetrics();
        // 2) Compute NOC from childrenMap for ALL components
        computeNOCGlobally();
        // 3) DIT is computed on-demand with recursion to capture any parents outside
        // the target set
    }

    private void computeMetrics() {
        // Loop over ALL components
        srcModel.components().filter(c -> c.componentType().isBaseComponent()).forEach(c -> {
            String id = c.uniqueName();

            // For NOC and DIT, we need to know who extends whom.
            // For each extension reference, let the parent know it has a child
            c.references(TypeReferences.EXTENSION).forEach(extRef -> {
                String parent = extRef.invokedComponent();
                String child = c.uniqueName();
                childrenMap.computeIfAbsent(parent, k -> new ArrayList<>()).add(child);
            });

            // Afferent Coupling (AC)
            c.references().forEach(ref -> {
                String target = ref.invokedComponent();
                if (!target.equals(c.uniqueName())) {
                    afferentReferences.merge(target, 1.0, Double::sum);
                }
            });

            // Process remaining metrics for target components only since they are
            // calculated locally.
            if (this.targetComponents.isEmpty() || this.targetComponents.contains(c.uniqueName())) {
                // Efferent Coupling (EC)
                efferentReferences.put(id, (double) c.references().size());

                // Weighted Method Complexity (WMC)
                double wmc = c.children().stream()
                        .map(srcModel::getComponent)
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .filter(ch -> ch.componentType() == ComponentType.METHOD)
                        .mapToDouble(Component::cyclo)
                        .sum();
                wmcValues.put(id, wmc);

                // Encapsulation ratio
                int total = c.children().size();
                if (total == 0) {
                    encapsulationValues.put(id, 0.0);
                } else {
                    long privateProtectedCount = c.children().stream()
                            .map(srcModel::getComponent)
                            .filter(Optional::isPresent)
                            .map(Optional::get)
                            .filter(ch -> ch.modifiers().contains("private")
                                    || ch.modifiers().contains("protected"))
                            .count();
                    encapsulationValues.put(id, (double) privateProtectedCount / total);
                }
            }
        });
    }

    private void computeNOCGlobally() {
        // NOC is just the size of the children list for each parent
        childrenMap.forEach((parentId, children) -> {
            subclasses.put(parentId, (double) children.size());
        });
    }

    // On-demand, recursive DIT (Depth of Inheritance Tree)
    // We do not filter by targetComponents here, because parents may lie outside
    // the target set
    private double computeDITInternal(String id) {
        // If already computed, return it
        if (ditValues.containsKey(id) && ditComputed.contains(id)) {
            return ditValues.get(id);
        }

        // Mark as in process
        ditComputed.add(id);

        // If no component or it is an interface => DIT = 1
        Optional<Component> cmpOpt = srcModel.getComponent(id);
        if (!cmpOpt.isPresent() || cmpOpt.get().componentType() == ComponentType.INTERFACE) {
            ditValues.put(id, 1.0);
            return 1.0;
        }

        Component cmp = cmpOpt.get();
        // DIT = 1 + max(DIT of parents)
        double maxInheritedDIT = cmp.references(TypeReferences.EXTENSION).stream()
                .map(ComponentReference::invokedComponent)
                .filter(srcModel::containsComponent)
                .mapToDouble(parentId -> 1.0 + computeDITInternal(parentId))
                .max().orElse(1.0);

        ditValues.put(id, maxInheritedDIT);
        return maxInheritedDIT;
    }

    // Public getter to handle DIT on-demand
    private double getDITOnDemand(String id) {
        if (!ditValues.containsKey(id) || !ditComputed.contains(id)) {
            return computeDITInternal(id);
        }
        return ditValues.get(id);
    }

    // --------------------------------------------------
    // Public APIs below
    // --------------------------------------------------

    public double getNOC(String id) {
        return subclasses.getOrDefault(id, 0.0);
    }

    // Use the on-demand approach
    public double getDIT(String id) {
        return getDITOnDemand(id);
    }

    public double getWMC(String id) {
        // If not computed (i.e., not in target set), default to 0
        return wmcValues.getOrDefault(id, 0.0);
    }

    public double getAfferentCoupling(String id) {
        // Ca was computed globally
        return afferentReferences.getOrDefault(id, 0.0);
    }

    public double getEfferentCoupling(String id) {
        // If not in target set, default 0
        return efferentReferences.getOrDefault(id, 0.0);
    }

    public double getEncapsulation(String id) {
        return encapsulationValues.getOrDefault(id, 0.0);
    }

    public OOPSourceCodeModel getSourceModel() {
        return srcModel;
    }
}
