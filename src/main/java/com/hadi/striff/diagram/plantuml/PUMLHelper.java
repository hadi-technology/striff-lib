package com.hadi.striff.diagram.plantuml;

import com.hadi.clarpse.sourcemodel.Component;
import com.hadi.striff.diagram.ComponentHelper;
import net.sourceforge.plantuml.FileFormat;
import net.sourceforge.plantuml.FileFormatOption;
import net.sourceforge.plantuml.SourceStringReader;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class PUMLHelper {

    /**
     * The identifier a component is drawn under.
     *
     * <p>A component's name is taken from its source, and for a language that names a module
     * after its file, from its path. A path can hold what PlantUML reads as syntax: a directory
     * called <code>{{project_slug}}</code>, a space, a quote, a letter outside ASCII. One such
     * character in one identifier fails the whole diagram. Each is written as an underscore, and
     * the identifier is closed with a number made from the name, so two names that differ only
     * in such a character are still two identifiers. A name holding none of them is written as
     * it always was.
     *
     * @param uniqueName the component's unique name
     * @return the identifier
     */
    public static String pumlId(String uniqueName) {
        // Strip generic type parameters (e.g., List<String> -> List) to avoid
        // PlantUML syntax issues with angle brackets
        String stripped = uniqueName.replaceAll("<[^>]*>", "");
        String id = stripped.replace(".", "-").replace(":", "-")
                .replace("(", "-").replace(")", "-");
        StringBuilder drawable = null;
        for (int at = 0; at < id.length(); at++) {
            if (!readAsSyntax(id.charAt(at))) {
                continue;
            }
            if (drawable == null) {
                drawable = new StringBuilder(id);
            }
            drawable.setCharAt(at, '_');
        }
        if (drawable == null) {
            return id;
        }
        return drawable.append('_').append(Integer.toUnsignedString(uniqueName.hashCode(), 16))
                .toString();
    }

    /** Whether PlantUML fails a diagram holding this character in an identifier. */
    private static boolean readAsSyntax(char character) {
        return character <= ' ' || character >= 127 || character == '"' || character == '<'
                || character == '>' || character == '{' || character == '}';
    }

    public static String pumlQualifiedId(Component component) {
        String namespace = ComponentHelper.packagePath(component.pkg());
        String id = pumlId(component.uniqueName());
        if (namespace == null || namespace.isEmpty()) {
            return id;
        }
        return namespace + "." + id;
    }

    public static String packageAlias(String packagePath) {
        String sanitized = packagePath.replaceAll("[^A-Za-z0-9_]", "_");
        return "pkg_" + sanitized + "_" + Integer.toUnsignedString(packagePath.hashCode(), 16);
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
