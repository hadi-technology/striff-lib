package com.hadi.striff.text;

import org.apache.commons.lang3.StringUtils;

/**
 * A piece of text with line breaks in it. Assumes input Text object does not
 * have any existing line breaks.
 *
 */
public class LineBreakedText implements Text {

    private final Text text;
    private final int maxCharsPerLine;

    public LineBreakedText(Text text, int maxCharsPerLine) {
        this.text = text;
        this.maxCharsPerLine = maxCharsPerLine;
    }

    @Override
    public String value() {
        String input = this.text.value();
        if (input == null || input.length() <= maxCharsPerLine) {
            return input == null ? "" : input;
        }
        // Simple word wrap implementation
        StringBuilder result = new StringBuilder();
        int currentLineLength = 0;
        String[] words = input.split("\\s+");
        for (int i = 0; i < words.length; i++) {
            String word = words[i];
            if (word.isEmpty()) {
                continue;
            }
            if (currentLineLength == 0) {
                result.append(word);
                currentLineLength = word.length();
            } else if (currentLineLength + 1 + word.length() <= maxCharsPerLine) {
                result.append(' ').append(word);
                currentLineLength += 1 + word.length();
            } else {
                result.append('\n').append(word);
                currentLineLength = word.length();
            }
        }
        return result.toString();
    }
}
