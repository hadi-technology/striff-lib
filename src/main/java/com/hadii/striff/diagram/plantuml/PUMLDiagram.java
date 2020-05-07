package com.hadii.striff.diagram.plantuml;

import com.hadii.striff.ChangeSet;
import com.hadii.striff.diagram.DiagramComponent;
import com.hadii.striff.diagram.scheme.DiagramColorScheme;
import com.hadii.striff.extractor.ComponentRelations;
import net.sourceforge.plantuml.FileFormat;
import net.sourceforge.plantuml.FileFormatOption;
import net.sourceforge.plantuml.SourceStringReader;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Map;
import java.util.Set;

public class PUMLDiagram {

    private final String classDiagramDescription;
    private final int size;
    private String svgText;

    public PUMLDiagram(final ChangeSet changeSet, final DiagramColorScheme colorScheme,
                       final Set<DiagramComponent> diagramComponents, Map<String, DiagramComponent> allComponents,
                       ComponentRelations allComponentRelations) throws Exception {
        this.classDiagramDescription = new PUMLClassDiagramDesc(
                changeSet, colorScheme, diagramComponents, allComponents, allComponentRelations).description();
        this.size = diagramComponents.size();
        generateSVGText();
    }

    private void generateSVGText() throws Exception {
        if (classDiagramDescription.isEmpty()) {
            this.svgText = "";
        } else {
            final long startTime = new Date().getTime();
            final String plantUMLString = genPlantUMLString();
            final byte[] diagram = generateDiagram(plantUMLString);
            final String diagramStr = new String(diagram);
            System.out.println(
                    " Clarity View diagram SVG text generated in " + (new Date().getTime() - startTime) + " milliseconds.");
            this.svgText = diagramStr;
            if (svgText.contains("Syntax Error")) {
                throw new Exception("A PUML syntax error occurred while generating this diagram!\n" + diagramStr);
            } else if (svgText.contains("An error has occured!")) {
                throw new Exception("A PUML diagram generation error occurred!\n" + diagramStr);
            }
        }
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

    /**
     * Invokes PlantUML to draw the class diagram based on the source string
     * representing a PlantUML compliant class diagram description.
     */
    private byte[] generateDiagram(String source) {
        final SourceStringReader reader = new SourceStringReader(source);
        final ByteArrayOutputStream os = new ByteArrayOutputStream();
        try {
            reader.generateImage(os, new FileFormatOption(FileFormat.SVG));
        } catch (

        final IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        try {
            os.close();
        } catch (final IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        // SvgGraphics.displayComponents.clear();
        return os.toByteArray();
    }
}
