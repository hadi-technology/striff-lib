package com.hadi.striff.diagram.plantuml;

import com.hadi.striff.annotations.LogExecutionTime;
import com.hadi.striff.diagram.ComponentHelper;
import com.hadi.striff.diagram.DiagramComponent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.regex.Matcher;

public class PUMLDiagram {

    private final String classDiagramDescription;
    private final Set<DiagramComponent> diagramComponents;
    private final int size;
    private final String svgText;
    private static final Logger LOGGER = LogManager.getLogger(PUMLDiagram.class);

    @LogExecutionTime
    public PUMLDiagram(PUMLDiagramData data) throws IOException, PUMLDrawException {
        LOGGER.info("Generating PlantUML diagram..");
        this.classDiagramDescription = new PUMLClassDiagramCode(data).code();
        this.diagramComponents = data.diagramCmps();
        this.size = this.diagramComponents.size();
        this.svgText = generateSVGText();
    }

    private String generateSVGText() throws PUMLDrawException, IOException {
        String diagramStr = "";
        if (!classDiagramDescription.isEmpty()) {
            final String plantUMLString = genPlantUMLString();
            final byte[] diagram = PUMLHelper.generateDiagram(plantUMLString);
            diagramStr = stripQualifiedPumlIds(new String(diagram, StandardCharsets.UTF_8));
            if (PUMLHelper.invalidPUMLDiagram(diagramStr)) {
                LOGGER.debug("Original PUML text:\n" + plantUMLString);
                LOGGER.debug("Generated diagram text:\n" + diagramStr);
                throw new PUMLDrawException("A PUML syntax error occurred while generating this "
                        + "diagram!");
            }
        }
        return diagramStr;
    }

    private String stripQualifiedPumlIds(String pumlGeneratedSVG) {
        if (diagramComponents == null || diagramComponents.isEmpty()) {
            return pumlGeneratedSVG;
        }
        String updatedSvg = pumlGeneratedSVG;
        for (DiagramComponent component : diagramComponents) {
            if (component == null || component.uniqueName() == null) {
                continue;
            }
            String packageName = ComponentHelper.packagePath(component.pkg());
            String pumlId = PUMLHelper.pumlId(component.uniqueName());
            String qualifiedId;
            if (packageName.isEmpty()) {
                qualifiedId = " ." + pumlId;
            } else {
                qualifiedId = PUMLHelper.packageAlias(packageName) + "." + pumlId;
            }

            updatedSvg = updatedSvg.replace(qualifiedId, pumlId);

            // Also replace data-qualified-name attributes with the original uniqueName
            // PlantUML generates data-qualified-name="com-example-MyClass"
            // We need data-qualified-name="com.example.MyClass" to match API componentId
            updatedSvg = updatedSvg.replace(
                    "data-qualified-name=\"" + pumlId + "\"",
                    "data-qualified-name=\"" + component.uniqueName() + "\""
            );
            updatedSvg = updatedSvg.replace(
                    "data-qualified-name=\"" + qualifiedId + "\"",
                    "data-qualified-name=\"" + component.uniqueName() + "\""
            );
            updatedSvg = updatedSvg.replaceAll(
                    "data-qualified-name=\"[^\"]*\\." + java.util.regex.Pattern.quote(pumlId) + "\"",
                    Matcher.quoteReplacement("data-qualified-name=\"" + component.uniqueName() + "\"")
            );
        }
        return updatedSvg;
    }

    public final String svgText() {
        return this.svgText;
    }

    public final int size() {
        return this.size;
    }

    /**
     * Returns a PlantUML compliant String representing the class diagram.
     */
    private String genPlantUMLString() {
        return this.classDiagramDescription;
    }

}
