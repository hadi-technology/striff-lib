package com.hadi.striff.diagram.plantuml;

import com.hadi.striff.annotations.LogExecutionTime;
import com.hadi.striff.diagram.ComponentHelper;
import com.hadi.striff.diagram.DiagramComponent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Set;

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
                qualifiedId = packageName + "." + pumlId;
            }

            updatedSvg = updatedSvg.replace(qualifiedId, pumlId);
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
