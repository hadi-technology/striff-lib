package com.hadi.striff.text;

import java.util.regex.Pattern;

final class DocCommentCharacterStrippedText implements Text {

    /**
     * A line comment marker at the start of a line: {@code //}, {@code ///} or {@code //!}. Only a
     * leading marker is removed, so a {@code //} inside a line, such as in a URL, is kept.
     */
    private static final Pattern LINE_COMMENT_MARKER = Pattern.compile("(?m)^[ \\t]*//+!?[ \\t]?");

    private final Text text;

    DocCommentCharacterStrippedText(Text text) {
        this.text = text;
    }

    @Override
    public String value() {
        return LINE_COMMENT_MARKER.matcher(this.text.value()).replaceAll("")
            .replace("/*", "")
            .replace("*/", "")
            .replace("*", "");
    }
}
