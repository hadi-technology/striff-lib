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
        Map<String, RawPackageNode> nodes = new LinkedHashMap<>();
        for (Map.Entry<String, String> entry : diagramDisplay.pkgColorMappings()) {
            String packagePath = entry.getKey();
            Set<DiagramComponent> pkgBaseCmps =
                    diagramCmps.stream()
                            .filter(cmp -> ComponentHelper.packagePath(cmp.pkg()).equals(packagePath)
                                    && cmp.componentType().isBaseComponent())
                            .collect(Collectors.toSet());
            ensureNodeWithAncestors(nodes, packagePath);
            RawPackageNode node = nodes.get(packagePath);
            node.color = entry.getValue();
            node.packageComponents = pkgBaseCmps;
        }

        List<PackageNode> roots = buildTree(nodes);
        StringBuilder stringBuilder = new StringBuilder();
        for (PackageNode root : roots) {
            appendNode(stringBuilder, data, root, null);
        }
        return stringBuilder.toString();
    }

    private void ensureNodeWithAncestors(Map<String, RawPackageNode> nodes, String packagePath) {
        if (packagePath == null || packagePath.isEmpty()) {
            nodes.computeIfAbsent(packagePath, RawPackageNode::new);
            return;
        }
        int start = 0;
        while (start < packagePath.length()) {
            int nextDot = packagePath.indexOf('.', start);
            String prefix = nextDot < 0 ? packagePath : packagePath.substring(0, nextDot);
            if (nextDot >= 0) {
                prefix = packagePath.substring(0, nextDot);
            } else {
                prefix = packagePath;
            }
            nodes.computeIfAbsent(prefix, RawPackageNode::new);
            if (nextDot < 0) {
                break;
            }
            start = nextDot + 1;
        }
    }

    private List<PackageNode> buildTree(Map<String, RawPackageNode> nodes) {
        List<RawPackageNode> roots = new ArrayList<>();
        for (RawPackageNode node : nodes.values()) {
            RawPackageNode parent = parentOf(node.packagePath, nodes);
            if (parent == null) {
                roots.add(node);
            } else {
                parent.children.add(node);
            }
        }
        Comparator<RawPackageNode> byPath = Comparator.comparing(
                pkg -> pkg.packagePath,
                Comparator.nullsFirst(String::compareTo));
        roots.sort(byPath);
        for (RawPackageNode node : nodes.values()) {
            node.children.sort(byPath);
        }
        return roots.stream()
                .map(this::compress)
                .collect(Collectors.toList());
    }

    private RawPackageNode parentOf(String packagePath, Map<String, RawPackageNode> nodes) {
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

    private PackageNode compress(RawPackageNode node) {
        List<PackageNode> compressedChildren = node.children.stream()
                .map(this::compress)
                .collect(Collectors.toList());
        boolean synthetic = node.color == null && (node.packageComponents == null || node.packageComponents.isEmpty());
        if (synthetic && compressedChildren.size() == 1) {
            return compressedChildren.get(0);
        }
        String color = node.color;
        if (color == null) {
            color = compressedChildren.isEmpty() ? "" : compressedChildren.get(0).color;
        }
        return new PackageNode(node.packagePath, color == null ? "" : color,
                node.packageComponents == null ? Set.of() : node.packageComponents, compressedChildren);
    }

    private void appendNode(StringBuilder builder, PUMLDiagramData data, PackageNode node, String parentPackagePath) {
        if (node.packagePath == null || node.packagePath.isEmpty()) {
            builder.append("package \" \"");
        } else {
            builder.append("package \"")
                    .append(displayLabel(node.packagePath, parentPackagePath))
                    .append("\" as ")
                    .append(PUMLHelper.packageAlias(node.packagePath));
        }
        builder.append(" ")
                .append(node.color)
                .append(" {\n")
                .append(new PUMLClassFieldsCode(data).value(node.packageComponents))
                .append(packageDecoratorsText(data, node.packagePath, node.packageComponents));
        for (PackageNode child : node.children) {
            appendNode(builder, data, child, node.packagePath);
        }
        builder.append("}\n");
    }

    private String displayLabel(String packagePath, String parentPackagePath) {
        if (packagePath == null || packagePath.isEmpty() || parentPackagePath == null || parentPackagePath.isEmpty()) {
            return packagePath;
        }
        String prefix = parentPackagePath + ".";
        if (!packagePath.startsWith(prefix)) {
            return packagePath;
        }
        return packagePath.substring(prefix.length());
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
        private final List<PackageNode> children;

        private PackageNode(String packagePath, String color, Set<DiagramComponent> packageComponents,
                List<PackageNode> children) {
            this.packagePath = packagePath;
            this.color = color;
            this.packageComponents = packageComponents;
            this.children = children;
        }
    }

    private static final class RawPackageNode {
        private final String packagePath;
        private String color;
        private Set<DiagramComponent> packageComponents = Set.of();
        private final List<RawPackageNode> children = new ArrayList<>();

        private RawPackageNode(String packagePath) {
            this.packagePath = packagePath;
        }
    }
}
