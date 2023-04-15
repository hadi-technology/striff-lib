package com.hadii.striff.diagram.plantuml;

import com.hadii.striff.diagram.DiagramComponent;
import com.hadii.striff.diagram.display.DiagramDisplay;
import com.hadii.striff.parse.CodeDiff;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Set;

public class PUMLDiagram {

    private final String classDiagramDescription;
    private final int size;
    private final String svgText;
    private static final Logger LOGGER = LogManager.getLogger(PUMLDiagram.class);

    public PUMLDiagram(final CodeDiff codeDiff, final Set<DiagramComponent> cmps,
                       final DiagramDisplay diagramDisplay) throws IOException, PUMLDrawException {
        LOGGER.info("Generating PlantUML diagram..");
        this.classDiagramDescription =
            new PUMLClassDiagramCode(codeDiff, diagramDisplay, cmps).code();
        this.size = cmps.size();
        this.svgText = generateSVGText();
    }

    private String generateSVGText() throws PUMLDrawException, IOException {
        String diagramStr = "";
        if (!classDiagramDescription.isEmpty()) {
            final long startTime = new Date().getTime();
            final String plantUMLString = genPlantUMLString();
            final byte[] diagram = PUMLHelper.generateDiagram(plantUMLString);
            diagramStr = decorateClassTextObjsWithCmpIds(new String(diagram, StandardCharsets.UTF_8));
            LOGGER.info("Striff Diagram SVG text was generated in "
                            + (new Date().getTime() - startTime) + " milliseconds.");
            if (PUMLHelper.invalidPUMLDiagram(diagramStr)) {
                LOGGER.debug("Original PUML text:\n" + plantUMLString);
                LOGGER.debug("Generated diagram text:\n" + diagramStr);
                throw new PUMLDrawException("A PUML syntax error occurred while generating this "
                                                + "diagram!");

            }
        }
        return diagramStr;
    }

    /**
     * Inserts component unique ids into their corresponding class text objects in the
     * given SVG diagram. For example, the following text object in the given SVG representing
     * a class name:
     * <p/>
     * "<text fill="#F8F8FF">InternalThreadLocalMap</text>"
     * <p/>
     * Might be replaced with
     *<p/>
     * "<text id="org.com.InternalThreadLocalMap" fill="#F8F8FF">InternalThreadLocalMap</text>"
     */
    private String decorateClassTextObjsWithCmpIds(String pumlGeneratedSVG) {
        String[] svgLines = pumlGeneratedSVG.split("\\r?\\n");
        for (int i = 0; i < svgLines.length; i++) {
            if (svgLines[i].startsWith("class ") && svgLines[i].contains("-->")) {
                String cmpUniqueName = svgLines[i].substring(6, svgLines[i].indexOf("-->"));
                svgLines[i] = svgLines[i].replaceFirst(
                    "<text ",
                    "<text id=\"" + cmpUniqueName + "\" "
                );
            }
        }
        return String.join(" ", svgLines);
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
