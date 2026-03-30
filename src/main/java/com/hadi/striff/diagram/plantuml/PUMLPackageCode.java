package com.hadi.striff.diagram.plantuml;

import com.hadi.striff.diagram.ComponentHelper;
import com.hadi.striff.diagram.DiagramComponent;
import com.hadi.striff.diagram.display.DiagramDisplay;
import com.hadi.striff.spi.PackageDecorator;
import com.hadi.striff.spi.PackageDecoratorContext;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class PUMLPackageCode {
    private final String code;

    PUMLPackageCode(PUMLDiagramData data) {
        this.code = this.generate(data);
    }

    private String generate(PUMLDiagramData data) {
        StringBuffer stringBuffer = new StringBuffer();
        DiagramDisplay diagramDisplay = data.diagramDisplay();
        Set<DiagramComponent> diagramCmps = data.diagramCmps();
        diagramDisplay.pkgColorMappings().forEach(entry -> {
            Set<DiagramComponent> pkgBaseCmps =
                diagramCmps.stream().filter(cmp -> ComponentHelper.packagePath(cmp.pkg()).equals(
                    entry.getKey()) && cmp.componentType().isBaseComponent())
                                 .collect(Collectors.toSet());
            if (entry.getKey() == null || entry.getKey().isEmpty()) {
                stringBuffer.append("package \" \"");
            } else {
                stringBuffer.append("package \"")
                        .append(entry.getKey())
                        .append("\" as ")
                        .append(PUMLHelper.packageAlias(entry.getKey()));
            }
            stringBuffer.append(" ")
                        .append(entry.getValue())
                        .append(" {\n")
                        .append(new PUMLClassFieldsCode(data).value(pkgBaseCmps))
                        .append(packageDecoratorsText(data, entry.getKey(), pkgBaseCmps))
                        .append("}\n");
        });
        return stringBuffer.toString();
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
}
