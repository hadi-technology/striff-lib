package com.hadii.striff.diagram.plantuml;

import com.hadii.striff.diagram.ComponentHelper;
import com.hadii.striff.diagram.DiagramComponent;
import com.hadii.striff.diagram.display.DiagramDisplay;
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
                // PUML namespaces cannot be empty, so we need to use the package keyword...
                stringBuffer.append("package ");
            } else {
                stringBuffer.append("namespace ");
            }
            stringBuffer.append(entry.getKey())
                        .append(" ")
                        .append(entry.getValue())
                        .append(" {\n")
                        .append(new PUMLClassFieldsCode(data).value(pkgBaseCmps))
                        .append("}\n");
        });
        return stringBuffer.toString();
    }

    public String value() {
        return this.code;
    }
}
