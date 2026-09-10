package com.hadi.striff.text;

/**
 * Text cut to a maximum number of visible characters, at a word boundary, ending in an ellipsis.
 *
 * <p>It runs before any PlantUML markup is added, so the cut can never land inside a bold marker or a
 * background tag: cutting the finished markup left an unclosed {@code **} and half a {@code </back>}
 * that PlantUML printed literally. An inline-code span the cut passes through is closed rather than
 * left open, and the span markers themselves are not counted as visible characters.
 */
final class TruncatedText implements Text {

    static final String ELLIPSIS = "...";

    private final Text text;
    private final int maxChars;

    TruncatedText(Text text, int maxChars) {
        this.text = text;
        this.maxChars = maxChars;
    }

    @Override
    public String value() {
        String input = text.value();
        if (input == null || visibleLength(input) <= maxChars) {
            return input;
        }
        int cut = cutIndex(input);
        String kept = input.substring(0, cut);
        if (!Character.isWhitespace(input.charAt(cut))) {
            int lastSpace = kept.lastIndexOf(' ');
            if (lastSpace > 0) {
                kept = kept.substring(0, lastSpace);
            }
        }
        kept = kept.stripTrailing();
        if (kept.lastIndexOf(InlineCodeMarkedText.INLINE_CODE_OPEN)
                > kept.lastIndexOf(InlineCodeMarkedText.INLINE_CODE_CLOSE)) {
            kept += InlineCodeMarkedText.INLINE_CODE_CLOSE;
        }
        return kept + ELLIPSIS;
    }

    /** The index of the first character past the budget that leaves room for the ellipsis. */
    private int cutIndex(String input) {
        int budget = maxChars - ELLIPSIS.length();
        int visible = 0;
        for (int i = 0; i < input.length(); i++) {
            if (!isMarker(input.charAt(i)) && ++visible > budget) {
                return i;
            }
        }
        return input.length();
    }

    private static int visibleLength(String input) {
        int visible = 0;
        for (int i = 0; i < input.length(); i++) {
            if (!isMarker(input.charAt(i))) {
                visible++;
            }
        }
        return visible;
    }

    private static boolean isMarker(char ch) {
        return InlineCodeMarkedText.INLINE_CODE_OPEN.charAt(0) == ch
                || InlineCodeMarkedText.INLINE_CODE_CLOSE.charAt(0) == ch;
    }
}
