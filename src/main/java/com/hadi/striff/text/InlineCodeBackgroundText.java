package com.hadi.striff.text;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class InlineCodeBackgroundText implements Text {

    private static final String INLINE_CODE_BACKGROUND = "#E6E6E6";
    private static final Pattern INLINE_CODE_PATTERN = Pattern.compile(
            Pattern.quote(InlineCodeMarkedText.INLINE_CODE_OPEN) + "(.*?)"
                    + Pattern.quote(InlineCodeMarkedText.INLINE_CODE_CLOSE),
            Pattern.DOTALL);

    private final Text text;

    InlineCodeBackgroundText(Text text) {
        this.text = text;
    }

    @Override
    public String value() {
        Matcher matcher = INLINE_CODE_PATTERN.matcher(text.value());
        StringBuilder builder = new StringBuilder();
        while (matcher.find()) {
            matcher.appendReplacement(builder,
                    Matcher.quoteReplacement("<back:" + INLINE_CODE_BACKGROUND + ">"
                            + matcher.group(1) + "</back>"));
        }
        matcher.appendTail(builder);
        return builder.toString();
    }
}
