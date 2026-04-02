package com.hadi.striff.text;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class InlineCodeMarkedText implements Text {

    static final String INLINE_CODE_OPEN = "\u0007";
    static final String INLINE_CODE_CLOSE = "\u0008";

    private static final List<Pattern> INLINE_CODE_PATTERNS = List.of(
            Pattern.compile("\\{@code\\s+([^}]+)}"),
            Pattern.compile("<(?:code|c|tt|kbd|samp)\\b[^>]*>(.*?)</(?:code|c|tt|kbd|samp)>",
                    Pattern.CASE_INSENSITIVE | Pattern.DOTALL),
            Pattern.compile(":code:`([^`\\r\\n]+)`"),
            Pattern.compile("``([^`\\r\\n]+)``"),
            Pattern.compile("`([^`\\r\\n]+)`"),
            Pattern.compile("\\\\[cp]\\s+([A-Za-z0-9_$.#]+(?:\\(\\))?)")
    );

    private final Text text;

    InlineCodeMarkedText(Text text) {
        this.text = text;
    }

    @Override
    public String value() {
        String markedText = text.value();
        for (Pattern pattern : INLINE_CODE_PATTERNS) {
            markedText = replaceMatches(markedText, pattern);
        }
        return markedText;
    }

    private static String replaceMatches(String input, Pattern pattern) {
        Matcher matcher = pattern.matcher(input);
        StringBuilder builder = new StringBuilder();
        while (matcher.find()) {
            matcher.appendReplacement(builder, Matcher.quoteReplacement(
                    INLINE_CODE_OPEN + matcher.group(1) + INLINE_CODE_CLOSE));
        }
        matcher.appendTail(builder);
        return builder.toString();
    }
}
