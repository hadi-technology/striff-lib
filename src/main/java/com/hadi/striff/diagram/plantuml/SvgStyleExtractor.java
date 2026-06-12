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

    // Matches an opening or self-closing tag that contains a style attribute.
    // Captures: (1) tag name + attrs before style, (2) style value, (3) attrs after style + closing
    // The negative lookbehind (?<!-) prevents matching "style" inside "font-style" etc.
    private static final Pattern TAG_WITH_STYLE = Pattern.compile(
            "(<\\w+\\s[^>]*?)(?<!-)\\bstyle=\"([^\"]*)\"([^>]*>)");

    private static final Pattern DECL = Pattern.compile(
            "\\s*([\\w-]+)\\s*:\\s*([^;]+)");

    private SvgStyleExtractor() {
    }

    /**
     * Converts inline {@code style} attributes to native SVG presentation
     * attributes on all elements. Non-convertible CSS properties (e.g.
     * {@code transform}, {@code background}) remain in the {@code style}
     * attribute. Properties that already exist as native attributes on the
     * element are skipped to avoid duplicate attributes.
     */
    public static String extractStyles(String svg) {
        Matcher matcher = TAG_WITH_STYLE.matcher(svg);
        if (!matcher.find()) {
            return svg;
        }
        matcher.reset();
        StringBuffer result = new StringBuffer();
        while (matcher.find()) {
            String beforeStyle = matcher.group(1);
            String styleValue = matcher.group(2);
            String afterStyle = matcher.group(3);

            Map<String, String> converted = new LinkedHashMap<>();
            StringBuilder remaining = new StringBuilder();

            Matcher declMatcher = DECL.matcher(styleValue);
            while (declMatcher.find()) {
                String prop = declMatcher.group(1).trim();
                String val = declMatcher.group(2).trim();
                if (CONVERTIBLE.contains(prop)) {
                    if (!hasNativeAttr(beforeStyle, afterStyle, prop)) {
                        converted.put(prop, val);
                    }
                } else {
                    if (remaining.length() > 0) {
                        remaining.append("; ");
                    }
                    remaining.append(prop).append(": ").append(val);
                }
            }

            StringBuilder replacement = new StringBuilder(beforeStyle);
            for (Map.Entry<String, String> e : converted.entrySet()) {
                replacement.append(e.getKey()).append("=\"").append(e.getValue()).append("\" ");
            }
            if (remaining.length() > 0) {
                replacement.append("style=\"").append(remaining).append("\"");
            }
            replacement.append(afterStyle);

            matcher.appendReplacement(result, Matcher.quoteReplacement(replacement.toString()));
        }
        matcher.appendTail(result);
        return result.toString();
    }

    private static boolean hasNativeAttr(String before, String after, String attrName) {
        String pattern = "\\b" + Pattern.quote(attrName) + "\\s*=";
        return Pattern.compile(pattern).matcher(before).find()
                || Pattern.compile(pattern).matcher(after).find();
    }
}
