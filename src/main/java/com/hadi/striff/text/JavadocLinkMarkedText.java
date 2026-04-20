package com.hadi.striff.text;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class JavadocLinkMarkedText implements Text {

    private static final Pattern JAVADOC_LINK_PATTERN = Pattern.compile(
            "\\{@link(?:plain)?\\s+([^}]*)}");

    private final Text text;

    JavadocLinkMarkedText(Text text) {
        this.text = text;
    }

    @Override
    public String value() {
        Matcher matcher = JAVADOC_LINK_PATTERN.matcher(text.value());
        StringBuilder builder = new StringBuilder();
        while (matcher.find()) {
            String displayValue = displayValue(matcher.group(1));
            matcher.appendReplacement(builder, Matcher.quoteReplacement(
                    InlineCodeMarkedText.INLINE_CODE_OPEN + displayValue + InlineCodeMarkedText.INLINE_CODE_CLOSE));
        }
        matcher.appendTail(builder);
        return builder.toString();
    }

    private static String displayValue(String linkBody) {
        if (linkBody == null) {
            return "";
        }
        String trimmed = linkBody.trim();
        if (trimmed.isEmpty()) {
            return trimmed;
        }
        int parenDepth = 0;
        int angleDepth = 0;
        int bracketDepth = 0;
        for (int i = 0; i < trimmed.length(); i++) {
            char ch = trimmed.charAt(i);
            switch (ch) {
                case '(' -> parenDepth++;
                case ')' -> {
                    int newDepth = parenDepth - 1;
                    parenDepth = Math.max(0, newDepth);
                }
                case '<' -> angleDepth++;
                case '>' -> {
                    int newDepth = angleDepth - 1;
                    angleDepth = Math.max(0, newDepth);
                }
                case '[' -> bracketDepth++;
                case ']' -> {
                    int newDepth = bracketDepth - 1;
                    bracketDepth = Math.max(0, newDepth);
                }
                default -> {
                    if (Character.isWhitespace(ch) && parenDepth == 0 && angleDepth == 0 && bracketDepth == 0) {
                        return trimmed.substring(i + 1).trim();
                    }
                }
            }
        }
        return trimmed;
    }
}
