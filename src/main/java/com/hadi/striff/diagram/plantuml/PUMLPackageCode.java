package com.hadi.striff.diagram.plantuml;

import com.hadi.striff.diagram.ComponentHelper;
import com.hadi.striff.diagram.DiagramComponent;
import com.hadi.striff.diagram.display.DiagramDisplay;
import com.hadi.striff.spi.PackageDecorator;
import com.hadi.striff.spi.PackageDecoratorContext;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class PUMLPackageCode {
    private final String code;

    PUMLPackageCode(PUMLDiagramData data) {
        this.code = this.generate(data);
    }

    private String generate(PUMLDiagramData data) {
        DiagramDisplay diagramDisplay = data.diagramDisplay();
        Set<DiagramComponent> diagramCmps = data.diagramCmps();
        Map<String, PackageNode> nodes = new LinkedHashMap<>();
        for (Map.Entry<String, String> entry : diagramDisplay.pkgColorMappings()) {
            String packagePath = entry.getKey();
            Set<DiagramComponent> pkgBaseCmps =
                    diagramCmps.stream()
                            .filter(cmp -> ComponentHelper.packagePath(cmp.pkg()).equals(packagePath)
                                    && cmp.componentType().isBaseComponent())
                            .collect(Collectors.toSet());
            nodes.put(packagePath, new PackageNode(packagePath, entry.getValue(), pkgBaseCmps));
        }

        List<PackageNode> roots = buildTree(nodes);
        StringBuilder stringBuilder = new StringBuilder();
        for (PackageNode root : roots) {
            appendNode(stringBuilder, data, root);
        }
        return stringBuilder.toString();
    }

    private List<PackageNode> buildTree(Map<String, PackageNode> nodes) {
        List<PackageNode> roots = new ArrayList<>();
        for (PackageNode node : nodes.values()) {
            PackageNode parent = parentOf(node.packagePath, nodes);
            if (parent == null) {
                roots.add(node);
            } else {
                parent.children.add(node);
            }
        }
        Comparator<PackageNode> byPath = Comparator.comparing(pkg -> pkg.packagePath);
        roots.sort(byPath);
        for (PackageNode node : nodes.values()) {
            node.children.sort(byPath);
        }
        return roots;
    }

    private PackageNode parentOf(String packagePath, Map<String, PackageNode> nodes) {
        if (packagePath == null || packagePath.isEmpty()) {
            return null;
        }
        String bestParent = null;
        for (String candidate : nodes.keySet()) {
            if (candidate == null || candidate.isEmpty() || candidate.equals(packagePath)) {
                continue;
            }
            if (packagePath.startsWith(candidate + ".")
                    && (bestParent == null || candidate.length() > bestParent.length())) {
                bestParent = candidate;
            }
        }
        if (bestParent == null) {
            return null;
        }
        return nodes.get(bestParent);
    }

    private void appendNode(StringBuilder builder, PUMLDiagramData data, PackageNode node) {
        if (node.packagePath == null || node.packagePath.isEmpty()) {
            builder.append("package \" \"");
        } else {
            builder.append("package \"")
                    .append(node.packagePath)
                    .append("\" as ")
                    .append(PUMLHelper.packageAlias(node.packagePath));
        }
        builder.append(" ")
                .append(node.color)
                .append(" {\n")
                .append(new PUMLClassFieldsCode(data).value(node.packageComponents))
                .append(packageDecoratorsText(data, node.packagePath, node.packageComponents));
        for (PackageNode child : node.children) {
            appendNode(builder, data, child);
        }
        builder.append("}\n");
    }

    private String packageDecoratorsText(PUMLDiagramData data, String packagePath, Set<DiagramComponent> packageComponents) {
        StringBuilder builder = new StringBuilder();
        PackageDecoratorContext context = new PackageDecoratorContext(
                packagePath,
                packageComponents,
                data.diagramCmps(),
                data.diagramDisplay());
        for (PackageDecorator decorator : data.packageDecorators()) {
            List<String> extra = decorator.decoratePackage(context);
            if (extra == null || extra.isEmpty()) {
                continue;
            }
            for (String line : extra) {
                builder.append(line);
                if (!line.endsWith("\n")) {
                    builder.append("\n");
                }
            }
        }
        return builder.toString();
    }

    public String value() {
        return this.code;
    }

    private static final class PackageNode {
        private final String packagePath;
        private final String color;
        private final Set<DiagramComponent> packageComponents;
        private final List<PackageNode> children = new ArrayList<>();

        private PackageNode(String packagePath, String color, Set<DiagramComponent> packageComponents) {
            this.packagePath = packagePath;
            this.color = color;
            this.packageComponents = packageComponents;
        }
    }
}
