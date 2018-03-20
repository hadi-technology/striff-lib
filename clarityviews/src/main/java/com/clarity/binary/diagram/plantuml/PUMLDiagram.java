package com.clarity.binary.diagram.plantuml;

import com.clarity.binary.diagram.Diagram;
import com.clarity.binary.diagram.scheme.DiagramColorScheme;
import net.sourceforge.plantuml.FileFormat;
import net.sourceforge.plantuml.FileFormatOption;
import net.sourceforge.plantuml.SourceStringReader;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Date;

public class PUMLDiagram implements Diagram {

    private static final String PLANT_UML_BEGIN_STRING = "@startuml\nskinparam linetype ortho\n";
    private static final String PLANT_UML_END_STRING = "\n@enduml";
    private PUMLDiagramDescription plantUMLClassDiagramDescription;
    private DiagramColorScheme colorScheme;
    private int size;
    private String svgText;

    public PUMLDiagram(final PUMLDiagramDescription plantUMLClassDescription, DiagramColorScheme colorScheme, int size) throws Exception {
        this.plantUMLClassDiagramDescription = plantUMLClassDescription;
        this.colorScheme = colorScheme;
        this.size = size;
        generateSVGText();
    }

    private void generateSVGText() throws Exception {
        if (plantUMLClassDiagramDescription.description().isEmpty()) {
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
                throw new Exception("A PUML syntax error occurred while generating this diagram!\n" + svgText);
            }
        }
    }
    @Override
    public final String svgText() {
        return this.svgText;
    }

    @Override
    public final int size() {
        return this.size;
    }

    /**
     * Forms a PlantUML compliant String representing the class diagram.
     */
    private String genPlantUMLString() {

        final String diagramSkin = formPlantUMLSkinString(colorScheme);

        final String source = PLANT_UML_BEGIN_STRING + diagramSkin + plantUMLClassDiagramDescription.description()
                + PLANT_UML_END_STRING;
        return source;
    }

    private String formPlantUMLSkinString(DiagramColorScheme colorScheme) {
        return "skinparam defaultFontName " + colorScheme.defaultFontName() + "\n" + "skinparam backgroundColor  "
                + colorScheme.backgroundColor() + "\n" + "\n" + "skinparam classArrowColor "
                + colorScheme.classArrowColor() + "\nskinparam legendBackgroundColor " + colorScheme.legendBackgroundColor() + "\nskinparam classBackgroundColor "
                + colorScheme.classBackgroundColor() + "\n" + "skinparam classArrowFontColor "
                + colorScheme.classArrowFontColor() + "\n" + "skinparam classArrowFontSize "
                + colorScheme.classArrowFontSize() + "\n" + "skinparam classFontColor "
                + colorScheme.classFontColor() + "\n" + "skinparam classFontSize " + colorScheme.classFontSize()
                + "\n" + "skinparam classStereotypeFontColor " + colorScheme.classStereotypeFontColor() + "\n"
                + "skinparam classAttributeFontColor " + colorScheme.classAttributeFontColor() + "\n"
                + "skinparam classAttributeFontSize " + colorScheme.classAttributeFontSize() + "\n"
                + "skinparam classFontName " + colorScheme.classFontName() + "\n" + "skinparam classAttributeFontName "
                + colorScheme.classAttributeFontName() + "\n" + "skinparam titleFontColor "
                + colorScheme.titleFontColor() + "\n" + "skinparam packageBackgroundColor "
                + colorScheme.packageBackgroundColor() + "\n" + "skinparam titleFontName " + colorScheme.titleFontName()
                + "\n" + "skinparam packageBorderColor " + colorScheme.packageBorderColor() + "\n"
                + "skinparam packageFontColor " + colorScheme.packageFontColor() + "\n" + "skinparam packageFontName "
                + colorScheme.packageFontName() + "\n" + "skinparam packageFontStyle " + colorScheme.packageFontStyle()
                + "\n" + "skinparam packageFontSize " + colorScheme.packageFontSize()
                + "\n" + "skinparam classBorderThickness " + colorScheme.classBorderThickness() + "\n"
                + "skinparam classHeaderBackgroundColor " + colorScheme.classHeaderBackgroundColor() + "\n"
                + "skinparam classBorderColor " + colorScheme.classBorderColor() + "\nskinparam minClassWidth "
                + colorScheme.minClassWidth() + "\n";
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
