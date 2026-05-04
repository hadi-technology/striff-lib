package com.hadi.striff.diagram.plantuml;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Post-processes SVG output to convert inline CSS {@code style} attributes into
 * native SVG presentation attributes. GitHub strips inline {@code style}
 * attributes from embedded SVGs, so this ensures stroke, fill, and other visual
 * properties survive GitHub's sanitizer.
 */
public final class SvgStyleExtractor {

    private static final Set<String> CONVERTIBLE = Set.of(
            "fill", "fill-opacity", "fill-rule",
            "stroke", "stroke-width", "stroke-dasharray", "stroke-dashoffset",
            "stroke-linecap", "stroke-linejoin", "stroke-miterlimit", "stroke-opacity",
            "opacity", "color",
            "font-family", "font-size", "font-style", "font-weight",
            "text-anchor", "text-decoration",
            "display", "visibility");

    private static final Pattern STYLE_ATTR = Pattern.compile(
            "\\bstyle=\"([^\"]*)\"");

    private static final Pattern DECL = Pattern.compile(
            "\\s*([\\w-]+)\\s*:\\s*([^;]+)");

    private SvgStyleExtractor() {
    }

    /**
     * Converts inline {@code style} attributes to native SVG presentation
     * attributes on all elements. Non-convertible CSS properties (e.g.
     * {@code transform}, {@code background}) remain in the {@code style}
     * attribute.
     */
    public static String extractStyles(String svg) {
        Matcher matcher = STYLE_ATTR.matcher(svg);
        if (!matcher.find()) {
            return svg;
        }
        matcher.reset();
        StringBuffer result = new StringBuffer();
        while (matcher.find()) {
            String styleValue = matcher.group(1);
            Map<String, String> converted = new LinkedHashMap<>();
            StringBuilder remaining = new StringBuilder();

            Matcher declMatcher = DECL.matcher(styleValue);
            while (declMatcher.find()) {
                String prop = declMatcher.group(1).trim();
                String val = declMatcher.group(2).trim();
                if (CONVERTIBLE.contains(prop)) {
                    converted.put(prop, val);
                } else {
                    if (remaining.length() > 0) {
                        remaining.append("; ");
                    }
                    remaining.append(prop).append(": ").append(val);
                }
            }

            StringBuilder replacement = new StringBuilder();
            for (Map.Entry<String, String> e : converted.entrySet()) {
                replacement.append(' ').append(e.getKey()).append("=\"").append(e.getValue()).append('"');
            }
            if (remaining.length() > 0) {
                replacement.append(" style=\"").append(remaining).append('"');
            }
            matcher.appendReplacement(result, Matcher.quoteReplacement(replacement.toString()));
        }
        matcher.appendTail(result);
        return result.toString();
    }
}
