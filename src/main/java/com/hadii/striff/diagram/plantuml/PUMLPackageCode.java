package com.hadii.striff.diagram.plantuml;

import com.hadii.striff.diagram.DiagramComponent;
import com.hadii.striff.diagram.display.DiagramDisplay;
import com.hadii.striff.parse.CodeDiff;

import java.util.Set;
import java.util.stream.Collectors;

final class PUMLPackageCode {


    private final String code;

    PUMLPackageCode(DiagramDisplay diagramDisplay, CodeDiff codeDiff,
                    Set<DiagramComponent> diagramComponents) {
        this.code = this.generate(diagramDisplay, codeDiff, diagramComponents);
    }

    private String generate(DiagramDisplay diagramDisplay, CodeDiff codeDiff,
                            Set<DiagramComponent> diagramComponents) {
        StringBuffer stringBuffer = new StringBuffer();
        diagramDisplay.pkgColorMappings().forEach(entry -> {
            Set<DiagramComponent> pkgBaseCmps =
                diagramComponents.stream().filter(cmp -> cmp.packagePath().equals(
                    entry.getKey()) && cmp.componentType().isBaseComponent())
                                 .collect(Collectors.toSet());
            if (entry.getKey() == null || entry.getKey().isEmpty()) {
                // PUML namespaces cannot be empty, so we need to use the package keyword...
                stringBuffer.append("package ");
            } else {
                stringBuffer.append("namespace ");
            }
            stringBuffer.append(entry.getKey())
                        .append(" ")
                        .append(entry.getValue())
                        .append(" {\n")
                        .append(new PUMLClassFieldsCode(codeDiff.components(),
                                                        codeDiff.changeSet(),
                                                        diagramDisplay).value(pkgBaseCmps))
                        .append("}\n");
        });
        return stringBuffer.toString();
    }

    public String value() {
        return this.code;
    }
}
