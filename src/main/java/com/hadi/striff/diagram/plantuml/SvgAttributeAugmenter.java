package com.hadi.striff.diagram.plantuml;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hadi.striff.diagram.DiagramComponent;

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Augments SVG with additional attributes to support browser extension functionality.
 * Specifically adds data-qualified-name attributes to entity elements.
 */
public class SvgAttributeAugmenter {

    private static final Logger LOGGER = LoggerFactory.getLogger(SvgAttributeAugmenter.class);

    /**
     * Pattern to match entity group elements in SVG.
     * Matches: <g id="entXXXX" ... class="entity" ...> ... </g>
     */
    private static final Pattern ENTITY_GROUP_PATTERN = Pattern.compile(
        "(<g\\s+id=\"ent\\d+\"[^>]*?class=\"[^\"]*entity[^\"]*\"[^>]*>)(.*?)(</g>)",
        Pattern.CASE_INSENSITIVE | Pattern.DOTALL
    );

    /**
     * Pattern to match text elements within entity groups to extract class/interface names.
     * Matches: <text ...>ClassName</text>
     */
    private static final Pattern TEXT_CONTENT_PATTERN = Pattern.compile(
        "<text[^>]*>([^<]+)</text>",
        Pattern.CASE_INSENSITIVE
    );

    /**
     * Augments SVG by adding data-qualified-name attributes to entity elements.
     * This allows the browser extension to map between SVG elements and components.
     *
     * @param svg The SVG string to augment
     * @param components The set of diagram components
     * @return The augmented SVG string
     */
    public static String addQualifiedNameAttributes(String svg, Set<DiagramComponent> components) {
        if (svg == null || svg.isEmpty()) {
            LOGGER.warn("SvgAttributeAugmenter: SVG is null or empty");
            return svg;
        }
        if (components == null || components.isEmpty()) {
            LOGGER.warn("SvgAttributeAugmenter: components is null or empty");
            return svg;
        }

        LOGGER.debug("SvgAttributeAugmenter: Starting with {} components", components.size());

        // Build a map of simple class names to original uniqueNames
        // SVG contains class names in text elements, we need to map them back to original uniqueNames
        java.util.Map<String, String> simpleNameToUniqueName = new java.util.HashMap<>();
        for (DiagramComponent component : components) {
            if (component == null || component.uniqueName() == null) {
                continue;
            }
            String uniqueName = component.uniqueName();
            // Extract simple name from the original uniqueName (before hyphen replacement)
            String simpleName = extractSimpleName(uniqueName);
            // Use simpleName as key, map to original uniqueName
            if (simpleName != null && !simpleName.isEmpty()) {
                // Handle potential collisions by preferring longer (more specific) names
                String existing = simpleNameToUniqueName.get(simpleName);
                if (existing == null || uniqueName.length() > existing.length()) {
                    simpleNameToUniqueName.put(simpleName, uniqueName);
                }
            }
        }

        if (simpleNameToUniqueName.isEmpty()) {
            LOGGER.warn("SvgAttributeAugmenter: No component names mapped, skipping SVG augmentation");
            return svg;
        }

        LOGGER.debug("SvgAttributeAugmenter: Built name map with {} entries", simpleNameToUniqueName.size());

        // Process SVG to find entity groups and add data-qualified-name attributes
        String augmented = augmentEntityGroups(svg, simpleNameToUniqueName);

        LOGGER.info("Augmented SVG with data-qualified-name attributes, name map keys: {}", simpleNameToUniqueName.keySet());

        return augmented;
    }

    /**
     * Augments entity groups in the SVG with data-qualified-name attributes.
     *
     * @param svg The SVG string to augment
     * @param simpleNameToUniqueName Map of simple names to original uniqueNames
     * @return The augmented SVG string
     */
    private static String augmentEntityGroups(String svg, java.util.Map<String, String> simpleNameToUniqueName) {
        Matcher matcher = ENTITY_GROUP_PATTERN.matcher(svg);
        StringBuffer result = new StringBuffer();
        int count = 0;

        LOGGER.info("Processing SVG, looking for {} entity groups", simpleNameToUniqueName.size());

        while (matcher.find()) {
            String groupStart = matcher.group(1); // <g ...>
            String groupContent = matcher.group(2); // content between <g> and </g>
            String groupEnd = matcher.group(3); // </g>

            // Extract class name from text content
            String className = extractClassNameFromGroup(groupContent);
            String qualifiedName = null;
            if (className != null) {
                qualifiedName = simpleNameToUniqueName.get(className);
                LOGGER.info("Found class name '{}' -> qualified name '{}'", className, qualifiedName);
            }

            // Build replacement with data-qualified-name attribute
            StringBuilder replacement = new StringBuilder(groupStart);

            // Insert data-qualified-name before the closing > of the opening tag
            // Use the qualified name as-is to match componentId from API response
            // Only add attributes for actual components, not package names
            if (qualifiedName != null && !isPackageName(qualifiedName)) {
                int insertPos = groupStart.lastIndexOf('>');
                if (insertPos > 0) {
                    replacement = new StringBuilder(groupStart.substring(0, insertPos));
                    // Don't replace dots or colons - keep qualifiedName as-is to match API componentId
                    // The browser extension matches data-qualified-name against componentId
                    replacement.append(" data-qualified-name=\"").append(escapeHtml(qualifiedName)).append("\">");
                    count++;
                }
            }

            replacement.append(groupContent).append(groupEnd);

            matcher.appendReplacement(result, replacement.toString());
        }
        matcher.appendTail(result);

        LOGGER.debug("Added data-qualified-name to {} entity groups", count);

        return result.toString();
    }

    /**
     * Checks if a qualified name represents a package (no class name) rather than a class/interface.
     * Packages in PlantUML can appear as entity groups but should not get data-qualified-name attributes.
     *
     * @param qualifiedName The fully qualified name to check
     * @return true if this appears to be a package name only
     */
    private static boolean isPackageName(String qualifiedName) {
        if (qualifiedName == null || qualifiedName.isEmpty()) {
            return true;
        }
        // Synthetic modules (prefixed with "module:") should be clickable
        if (qualifiedName.startsWith("module:")) {
            return false;
        }
        // Get the last segment after the final dot
        int lastDot = qualifiedName.lastIndexOf('.');
        if (lastDot < 0) {
            // No dot means it's a simple class name (not a package)
            return false;
        }
        String lastSegment = qualifiedName.substring(lastDot + 1);
        // If the last segment starts with a lowercase letter, it's likely a package
        return Character.isLowerCase(lastSegment.charAt(0));
    }

    /**
     * Extracts the class/interface name from an entity group's content.
     */
    private static String extractClassNameFromGroup(String groupContent) {
        // Look for text elements containing the class name
        // Typically the class name appears in a text element with specific positioning
        java.util.regex.Matcher textMatcher = TEXT_CONTENT_PATTERN.matcher(groupContent);
        String bestCandidate = null;
        while (textMatcher.find()) {
            String text = textMatcher.group(1).trim();
            // Filter out common non-class text content
            final boolean isValidClassName = !text.isEmpty()
                && !text.equals(" ")
                && !text.matches("\\d+")
                && !text.startsWith("..")
                && !text.startsWith("--")
                && !text.startsWith("()")
                && !text.startsWith(".")  // Not starting with a dot (package-like names)
                && !text.contains("-")  // Hyphens indicate PlantUML internal IDs, not real class names
                && !text.matches(".+\\..+\\..+")  // Not multiple dots (qualified names that shouldn't appear)
                && text.length() > 1
                && text.length() < 100  // Sanity check - class names aren't this long
                && Character.isJavaIdentifierStart(text.charAt(0));
            if (isValidClassName) {
                // Prefer shorter names (the actual class name vs longer metadata)
                if (bestCandidate == null || text.length() < bestCandidate.length()) {
                    bestCandidate = text;
                }
            }
            if (text.contains("-") || text.contains("module:")) {
                LOGGER.info("Filtered out text: '{}'", text);
            }
        }
        if (bestCandidate != null) {
            LOGGER.info("Extracted class name: '{}' from group content", bestCandidate);
        } else {
            LOGGER.warn("No valid class name found in group content (first 200 chars): {}",
                    groupContent.substring(0, Math.min(200, groupContent.length())));
        }
        return bestCandidate;
    }

    /**
     * Extracts the simple class name from a fully qualified name.
     * Handles both dot-separated (Java style) and hyphen-separated (PlantUML style) names.
     */
    private static String extractSimpleName(String qualifiedName) {
        if (qualifiedName == null || qualifiedName.isEmpty()) {
            return null;
        }
        // First try dot separator (for original unique names like "com.example.MyClass")
        int lastDot = qualifiedName.lastIndexOf('.');
        if (lastDot >= 0 && lastDot < qualifiedName.length() - 1) {
            return qualifiedName.substring(lastDot + 1);
        }
        // If no dots, try hyphen separator (for PlantUML hyphenated names like "com-example-MyClass")
        int lastHyphen = qualifiedName.lastIndexOf('-');
        if (lastHyphen >= 0 && lastHyphen < qualifiedName.length() - 1) {
            return qualifiedName.substring(lastHyphen + 1);
        }
        return qualifiedName;
    }

    /**
     * Escapes special HTML characters in attribute values.
     */
    private static String escapeHtml(String value) {
        if (value == null) {
            return "";
        }
        return value
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&apos;");
    }
}
