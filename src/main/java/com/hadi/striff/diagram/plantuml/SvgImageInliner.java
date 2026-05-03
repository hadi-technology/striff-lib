package com.hadi.striff.diagram.plantuml;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Makes SVG output compatible with GitHub's sanitizer by:
 * <ol>
 *   <li>Converting {@code <image>} elements with {@code data:image/svg+xml;base64}
 *       data URIs into inline SVG groups.</li>
 *   <li>Flattening CSS {@code style} attributes into individual SVG presentation
 *       attributes (stroke, stroke-width, etc.) since GitHub strips
 *       {@code style} attributes.</li>
 * </ol>
 */
public final class SvgImageInliner {

    private static final Logger LOGGER = LoggerFactory.getLogger(SvgImageInliner.class);

    private static final Pattern IMAGE_PATTERN = Pattern.compile(
            "<image\\s+([^>]*?)(?:xlink:)?href\\s*=\\s*"
                    + "\"data:image/svg\\+xml;base64,([A-Za-z0-9+/=]+)\"([^>]*?)/>",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    private static final Pattern X_ATTR = Pattern.compile("\\bx\\s*=\\s*\"([^\"]+)\"");
    private static final Pattern Y_ATTR = Pattern.compile("\\by\\s*=\\s*\"([^\"]+)\"");
    private static final Pattern WIDTH_ATTR = Pattern.compile("\\bwidth\\s*=\\s*\"([^\"]+)\"");
    private static final Pattern HEIGHT_ATTR = Pattern.compile("\\bheight\\s*=\\s*\"([^\"]+)\"");
    private static final Pattern ID_ATTR = Pattern.compile("\\bid\\s*=\\s*\"([^\"]+)\"");

    private static final Pattern SVG_TAG = Pattern.compile(
            "<svg[^>]*>(.*)</svg>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    /**
     * Matches CSS style attributes containing only stroke/stroke-width
     * properties (the pattern PlantUML generates for SVG elements).
     * Captures the stroke color and width values.
     */
    private static final Pattern STROKE_STYLE = Pattern.compile(
            "\\s*style\\s*=\\s*\"stroke:([^;]+);stroke-width:([^;\"]+);?\"");

    private SvgImageInliner() {
    }

    /**
     * Makes the SVG GitHub-compatible by inlining embedded images
     * and flattening CSS style attributes.
     *
     * @param svg the SVG string to process
     * @return the GitHub-compatible SVG
     */
    public static String inlineSvgImages(String svg) {
        if (svg == null || svg.isEmpty()) {
            return svg;
        }
        String result = inlineImageDataUris(svg);
        result = flattenStyleAttributes(result);
        return result;
    }

    /**
     * Converts {@code <image>} elements with SVG data URIs to inline groups.
     */
    private static String inlineImageDataUris(String svg) {
        Matcher matcher = IMAGE_PATTERN.matcher(svg);
        StringBuffer result = new StringBuffer();
        int idCounter = 0;

        while (matcher.find()) {
            String preHrefAttrs = matcher.group(1);
            String base64Payload = matcher.group(2);
            String postHrefAttrs = matcher.group(3);
            String allAttrs = preHrefAttrs + " " + postHrefAttrs;

            String replacement;
            try {
                replacement = buildInlineReplacement(
                        base64Payload, allAttrs, idCounter);
                idCounter++;
            } catch (Exception e) {
                LOGGER.debug("Failed to inline SVG image, keeping "
                        + "original: {}", e.getMessage());
                replacement = Matcher.quoteReplacement(matcher.group(0));
            }
            matcher.appendReplacement(result, replacement);
        }
        matcher.appendTail(result);
        return result.toString();
    }

    /**
     * Converts CSS {@code style} attributes containing stroke properties
     * into individual SVG presentation attributes. PlantUML generates
     * elements with {@code style="stroke:X;stroke-width:Y;"} which
     * GitHub's sanitizer strips, making lines invisible.
     */
    static String flattenStyleAttributes(String svg) {
        if (svg == null || svg.isEmpty()) {
            return svg;
        }
        Matcher matcher = STROKE_STYLE.matcher(svg);
        return matcher.replaceAll(" stroke=\"$1\" stroke-width=\"$2\"");
    }

    private static String buildInlineReplacement(String base64Payload,
            String attrs, int idIndex) {
        byte[] decoded = Base64.getDecoder().decode(base64Payload);
        String innerSvg = new String(decoded, StandardCharsets.UTF_8);

        String x = extractAttr(attrs, X_ATTR, "0");
        String y = extractAttr(attrs, Y_ATTR, "0");
        String imgWidth = extractAttr(attrs, WIDTH_ATTR, null);
        String imgHeight = extractAttr(attrs, HEIGHT_ATTR, null);

        double[] innerDims = extractInnerSvgDimensions(innerSvg);
        double innerW = innerDims[0];
        double innerH = innerDims[1];

        String innerContent = extractInnerContent(innerSvg);
        innerContent = deduplicateIds(innerContent, idIndex);

        String scaleTransform = computeScale(imgWidth, imgHeight, innerW, innerH);

        StringBuilder sb = new StringBuilder();
        sb.append("<g transform=\"translate(").append(x).append(",").append(y).append(")");
        if (!scaleTransform.isEmpty()) {
            sb.append(" ").append(scaleTransform);
        }
        sb.append("\">");
        sb.append(innerContent);
        sb.append("</g>");
        return Matcher.quoteReplacement(sb.toString());
    }

    private static String extractAttr(String attrs, Pattern attrPattern, String defaultValue) {
        Matcher m = attrPattern.matcher(attrs);
        if (m.find()) {
            return m.group(1);
        }
        return defaultValue;
    }

    private static double[] extractInnerSvgDimensions(String innerSvg) {
        double[] dims = {1.0, 1.0};
        Matcher wMatcher = WIDTH_ATTR.matcher(innerSvg);
        if (wMatcher.find()) {
            dims[0] = parseNumericValue(wMatcher.group(1));
        }
        Matcher hMatcher = HEIGHT_ATTR.matcher(innerSvg);
        if (hMatcher.find()) {
            dims[1] = parseNumericValue(hMatcher.group(1));
        }
        return dims;
    }

    private static double parseNumericValue(String value) {
        if (value == null || value.isEmpty()) {
            return 1.0;
        }
        try {
            StringBuilder num = new StringBuilder();
            for (char c : value.toCharArray()) {
                if (Character.isDigit(c) || c == '.' || c == '-' || c == '+') {
                    num.append(c);
                } else {
                    break;
                }
            }
            if (num.length() > 0) {
                return Double.parseDouble(num.toString());
            }
        } catch (NumberFormatException e) {
            LOGGER.debug("Could not parse numeric value: {}", value);
        }
        return 1.0;
    }

    private static String extractInnerContent(String innerSvg) {
        // Strip the outer <svg> tag and get inner content
        Matcher svgMatcher = SVG_TAG.matcher(innerSvg);
        if (svgMatcher.find()) {
            return svgMatcher.group(1).trim();
        }
        // Fallback: strip just the opening and closing tags
        String content = innerSvg;
        int startIdx = content.indexOf('>');
        int endIdx = content.lastIndexOf("</svg>");
        if (startIdx >= 0 && endIdx > startIdx) {
            content = content.substring(startIdx + 1, endIdx).trim();
        }
        return content;
    }

    private static String deduplicateIds(String content, int index) {
        Set<String> ids = new LinkedHashSet<>();
        Matcher idMatcher = ID_ATTR.matcher(content);
        while (idMatcher.find()) {
            ids.add(idMatcher.group(1));
        }
        if (ids.isEmpty()) {
            return content;
        }

        String result = content;
        for (String id : ids) {
            String newId = "i_" + index + "_" + id;
            result = result.replaceAll(
                    "\\bid\\s*=\\s*\"" + Pattern.quote(id) + "\"",
                    Matcher.quoteReplacement("id=\"" + newId + "\""));
            result = result.replaceAll(
                    "url\\(\\s*#" + Pattern.quote(id) + "\\s*\\)",
                    Matcher.quoteReplacement("url(#" + newId + ")"));
        }
        return result;
    }

    private static String computeScale(String imgWidth, String imgHeight,
            double innerW, double innerH) {
        if (imgWidth == null || imgHeight == null || innerW <= 0 || innerH <= 0) {
            return "";
        }
        double targetW = parseNumericValue(imgWidth);
        double targetH = parseNumericValue(imgHeight);
        double scaleX = targetW / innerW;
        double scaleY = targetH / innerH;

        // Only add scale if it's meaningfully different from 1.0
        if (Math.abs(scaleX - 1.0) < 0.001 && Math.abs(scaleY - 1.0) < 0.001) {
            return "";
        }
        return String.format("scale(%.4f,%.4f)", scaleX, scaleY);
    }
}
