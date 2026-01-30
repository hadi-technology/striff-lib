package com.hadi.striff.diagram.plantuml;

import com.hadi.clarpse.sourcemodel.Component;
import com.hadi.striff.diagram.ComponentHelper;
import net.sourceforge.plantuml.FileFormat;
import net.sourceforge.plantuml.FileFormatOption;
import net.sourceforge.plantuml.SourceStringReader;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class PUMLHelper {

    public static String pumlId(String uniqueName) {
        return uniqueName.replace(".", "-");
    }

    public static String pumlQualifiedId(Component component) {
        String namespace = ComponentHelper.packagePath(component.pkg());
        String id = pumlId(component.uniqueName());
        if (namespace == null || namespace.isEmpty()) {
            return id;
        }
        return namespace + "." + id;
    }

    /**
     * Invokes PlantUML to draw the class diagram based on the source string
     * representing a PlantUML compliant class diagram code.
     */
    public static byte[] generateDiagram(String source) throws IOException, PUMLDrawException {
        final SourceStringReader reader = new SourceStringReader(source);
        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            reader.outputImage(os, new FileFormatOption(FileFormat.SVG));
            return os.toByteArray();
        } catch (final Exception e) {
            throw new PUMLDrawException("Error occurred while generating diagram!", e);
        }
    }

    public static boolean invalidPUMLDiagram(String svgCode) throws PUMLDrawException {
        return svgCode.contains("Syntax Error") || svgCode.contains("An error has")
                || svgCode.contains("[From string (line");
    }
}
