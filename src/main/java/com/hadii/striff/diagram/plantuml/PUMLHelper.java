package com.hadii.striff.diagram.plantuml;

import net.sourceforge.plantuml.FileFormat;
import net.sourceforge.plantuml.FileFormatOption;
import net.sourceforge.plantuml.SourceStringReader;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class PUMLHelper {

    /**
     * Invokes PlantUML to draw the class diagram based on the source string
     * representing a PlantUML compliant class diagram code.
     */
    public static byte[] generateDiagram(String source) throws IOException, PUMLDrawException {
        final SourceStringReader reader = new SourceStringReader(source);
        final ByteArrayOutputStream os = new ByteArrayOutputStream();
        try {
            reader.outputImage(os, new FileFormatOption(FileFormat.SVG));
        } catch (final Exception e) {
            throw new PUMLDrawException("Error occurred while generating diagram!", e);
        } finally {
            os.close();
        }
        return os.toByteArray();
    }

    public static boolean invalidPUMLDiagram(String svgCode) throws PUMLDrawException {
        return svgCode.contains("Syntax Error") || svgCode.contains("An error has")
                || svgCode.contains("[From string (line");
    }
}
