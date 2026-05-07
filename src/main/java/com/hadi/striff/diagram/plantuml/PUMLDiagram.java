package com.hadi.striff.diagram.plantuml;

import com.hadi.striff.annotations.LogExecutionTime;
import com.hadi.striff.diagram.ComponentHelper;
import com.hadi.striff.diagram.DiagramComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PUMLDiagram {

    private final String classDiagramDescription;
    private final Set<DiagramComponent> diagramComponents;
    private final int size;
    private final String svgText;
    private static final Logger LOGGER = LoggerFactory.getLogger(PUMLDiagram.class);

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
            diagramStr = SvgImageInliner.inlineSvgImages(
                    sanitizeXml(
                        stripQualifiedPumlIds(new String(diagram, StandardCharsets.UTF_8))));
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
     * Removes anything from the SVG that is not a legal XML 1.0 character, in a single pass:
     * <ul>
     *   <li>Character references ({@code &#8;}, {@code &#x1A;}) that resolve to invalid codepoints</li>
     *   <li>Raw control characters and other illegal codepoints</li>
     * </ul>
     * These can originate from inline-code delimiters, source Javadoc, or PlantUML itself.
     * <p>
     * Valid XML 1.0: {@code #x9 | #xA | #xD | [#x20-#xD7FF] | [#xE000-#xFFFD] | [#x10000-#x10FFFF]}
     */
    private static final Pattern CHAR_REF = Pattern.compile("&#(?:([0-9]+)|x([0-9a-fA-F]+));");

    private static String sanitizeXml(String svg) {
        StringBuilder sb = new StringBuilder(svg.length());
        Matcher m = CHAR_REF.matcher(svg);
        int last = 0;
        while (m.find()) {
            appendValidChars(sb, svg, last, m.start());
            int val = parseCharRef(m);
            if (val >= 0 && isValidXmlChar(val)) {
                sb.append(m.group());
            }
            last = m.end();
        }
        appendValidChars(sb, svg, last, svg.length());
        return sb.toString();
    }

    private static int parseCharRef(Matcher m) {
        try {
            if (m.group(1) != null) {
                return Integer.parseInt(m.group(1));
            }
            return Integer.parseInt(m.group(2), 16);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private static void appendValidChars(StringBuilder sb, String s, int start, int end) {
        for (int i = start; i < end;) {
            int cp = s.codePointAt(i);
            if (isValidXmlChar(cp)) {
                sb.appendCodePoint(cp);
            }
            i += Character.charCount(cp);
        }
    }

    private static boolean isValidXmlChar(int cp) {
        return cp == 0x9 || cp == 0xA || cp == 0xD
                || (cp >= 0x20 && cp <= 0xD7FF)
                || (cp >= 0xE000 && cp <= 0xFFFD)
                || (cp >= 0x10000 && cp <= 0x10FFFF);
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

    public final String pumlSource() {
        return this.classDiagramDescription;
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
